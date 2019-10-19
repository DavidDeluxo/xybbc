package com.xingyun.bbc.mall.service.impl;

import com.google.common.collect.Lists;
import com.xingyun.bbc.common.redis.XyIdGenerator;
import com.xingyun.bbc.common.redis.order.OrderTypeEnum;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.BankDepositApi;
import com.xingyun.bbc.core.operate.po.BankDeposit;
import com.xingyun.bbc.core.order.api.OrderPaymentApi;
import com.xingyun.bbc.core.order.po.OrderPayment;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.user.api.*;
import com.xingyun.bbc.core.user.po.*;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.core.utils.StringUtil;
import com.xingyun.bbc.mall.base.enums.MallResultStatus;
import com.xingyun.bbc.mall.base.utils.EncryptUtils;
import com.xingyun.bbc.mall.base.utils.MD5Util;
import com.xingyun.bbc.mall.base.utils.PriceUtil;
import com.xingyun.bbc.mall.base.utils.RandomUtils;
import com.xingyun.bbc.mall.common.constans.MallRedisConstant;
import com.xingyun.bbc.mall.common.ensure.Ensure;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.common.lock.XybbcLock;
import com.xingyun.bbc.mall.model.dto.WithdrawDto;
import com.xingyun.bbc.mall.model.dto.WithdrawRateDto;
import com.xingyun.bbc.mall.model.vo.BanksVo;
import com.xingyun.bbc.mall.model.vo.WalletAmountVo;
import com.xingyun.bbc.mall.model.vo.WithdrawRateVo;
import com.xingyun.bbc.mall.service.WalletService;
import com.xingyun.bbc.pay.api.AliPayApi;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.xingyun.bbc.mall.common.enums.OrderPayMent.OrderPayStatusEnum.*;


/**
 * @author hekaijin
 * @date 2019/9/16 16:34
 * @Description
 */
@Slf4j
@Validated
@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private UserApi userApi;
    @Autowired
    private UserAccountApi userAccountApi;
    @Autowired
    private OrderPaymentApi orderPaymentApi;
    @Autowired
    private UserWithdrawRateApi userWithdrawRateApi;
    @Autowired
    private BankDepositApi bankDepositApi;
    @Autowired
    private UserAccountTransApi userAccountTransApi;
    @Autowired
    private UserAccountTransWaterApi userAccountTransWaterApi;
    @Autowired
    private UserAccountWaterApi userAccountWaterApi;
    @Autowired
    private AliPayApi aliPayApi;
    @Autowired
    private XybbcLock xybbcLock;

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private static final BigDecimal ONE_MILLION = new BigDecimal("1000000");

    @Override
    public WalletAmountVo queryAmount(Long uid) {

        Result<UserAccount> accountResult = userAccountApi.queryById(uid);
        if (!accountResult.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);

        WalletAmountVo amountVo = new WalletAmountVo();

        UserAccount account = accountResult.getData();
        if (null == account) return amountVo;

        amountVo.setBalance(PriceUtil.toYuan(account.getFbalance()));

        Criteria<OrderPayment, Object> op = Criteria.of(OrderPayment.class);

        op.andEqualTo(OrderPayment::getFuid, uid);
        op.andIn(OrderPayment::getForderStatus, Arrays.asList(TO_BE_DELIVERED.getValue(), PENDING_RECEIPT.getValue(), RECEIVED.getValue(), DONE.getValue()));
        Result<List<OrderPayment>> orderListRes = orderPaymentApi.queryByCriteria(op);

        if (!orderListRes.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);

        List<OrderPayment> orderList = orderListRes.getData();
        if (CollectionUtils.isEmpty(orderList)) return amountVo;

        long sum = orderList.stream().mapToLong(OrderPayment::getFtotalAgentIncome).sum();


        return amountVo.setWaitIncome(PriceUtil.toYuan(sum));
    }

    @Override
    public Boolean checkPayPwd(Long uid) {
        User user = this.checkUser(uid);
        return !StringUtil.isBlank(user.getFwithdrawPasswd());
    }

    @Override
    public List<WithdrawRateVo> queryWithdrawRate(WithdrawRateDto rateDto) {

        if (null != rateDto && null != rateDto.getFwithdrawType()) {

            Date today = new Date();

            Criteria<UserWithdrawRate, Object> rateQuery = Criteria.of(UserWithdrawRate.class);

            rateQuery.andEqualTo(UserWithdrawRate::getFwithdrawType, rateDto.getFwithdrawType());
            rateQuery.andEqualTo(UserWithdrawRate::getFstate, 1);
            rateQuery.andLessThanOrEqualTo(UserWithdrawRate::getFeffectiveDate, today);
            rateQuery.andGreaterThanOrEqualTo(UserWithdrawRate::getFinvalidDate, today);

            Result<List<UserWithdrawRate>> userRateRes = userWithdrawRateApi.queryByCriteria(rateQuery);

            if (!userRateRes.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);

            if (CollectionUtils.isEmpty(userRateRes.getData()))
                throw new BizException(MallResultStatus.USER_WITHDRAW_RATE_NOT_CONFIG);

            return this.toWithdrawRateVos(userRateRes.getData());
        }

        Result<List<UserWithdrawRate>> result = userWithdrawRateApi.queryAll();
        if (!result.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);

        if (CollectionUtils.isEmpty(result.getData()))
            throw new BizException(MallResultStatus.USER_WITHDRAW_RATE_NOT_CONFIG);

        return this.toWithdrawRateVos(result.getData());
    }

    @Override
    public List<BanksVo> queryBankList() {
        Result<List<BankDeposit>> result = bankDepositApi.queryAll();
        if (!result.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);

        if (CollectionUtils.isEmpty(result.getData())) throw new BizException(MallResultStatus.BANK_NOT_CONFIG);

        return result.getData().stream()
                .map(bank -> new BanksVo()
                        .setIcon(bank.getFicon())
                        .setBankCode(bank.getFbankCode())
                        .setBankName(bank.getFbankName()))
                .collect(Collectors.toList());
    }

    @Override
    @GlobalTransactional
    public Boolean withdraw(@Valid WithdrawDto withdrawDto) {

        String withdrawLockKey = this.getWithdrawKey(withdrawDto);

        String withdrawLockValue = RandomUtils.getUUID();

        try {

            Ensure.that(xybbcLock.tryLock(withdrawLockKey, withdrawLockValue, 300)).isTrue(MallExceptionCode.WITHDRAW_PROCESSING);

            return this.invokeWithdraw(withdrawDto);

        } finally {

            xybbcLock.releaseLock(withdrawLockKey, withdrawLockValue);
        }
    }

    private boolean invokeWithdraw(@Valid WithdrawDto withdrawDto) {
        // 校验
        Long uid = this.withdrawCheck(withdrawDto);

        // 获取提现费率
        WithdrawRate withdrawRate = new WithdrawRate(withdrawDto.getWay()).get();

        BigDecimal transAmount = withdrawDto.getWithdrawAmount();

        // build UserAccountTrans
        AccountTrans accountTrans = new AccountTrans(withdrawDto, uid, withdrawRate, transAmount).build();

        // 生成订单号
        String transId = XyIdGenerator.generateId(OrderTypeEnum.WITHDRAW_ORDER.getCode());

        // 判断金额是否正确(提现、余额金额不能小于等于0，更新后的余额、冻结金额不能小于0)
        CheckAfterMoney checkAfterMoney = new CheckAfterMoney(uid, transAmount).check();

        // 提现申请插入数据库
        this.addAccountTrans(accountTrans.getUserAccountTrans(), transId);

        log.info("|生成提现订单|用户id:{}|订单号:{}|", uid, transId);

        log.info("|用户id:{}|提现金额:{}|手续费:{}|", uid, PriceUtil.toYuan(transAmount), PriceUtil.toYuan(accountTrans.getFeeAmount()));

        // 申请数据插入流水表
        this.addAccountTransWater(transId);

        // 修改用户账户表，冻结提现金额
        this.modifyAccount(uid, transAmount, checkAfterMoney.getNewBalance(), checkAfterMoney.getFreezeWithdraw());

        log.info("|冻结提现金额:{}|用户id:{}|", PriceUtil.toYuan(transAmount), uid);

        // 添加账户流水
        this.addAccountWater(uid);

        return true;
    }

    private void addAccountWater(Long uid) {
        Result<UserAccount> userAccountResult = userAccountApi.queryById(uid);
        if (!userAccountResult.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        if (null == userAccountResult.getData()) throw new BizException(ResultStatus.NOT_IMPLEMENTED);

        UserAccountWater accountWater = new UserAccountWater();
        BeanUtils.copyProperties(userAccountResult.getData(), accountWater);
        Result<Integer> waterResult = userAccountWaterApi.create(accountWater);
        if (!waterResult.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        if (waterResult.getData() < 0) throw new BizException(ResultStatus.NOT_IMPLEMENTED);
    }

    private void modifyAccount(Long uid, BigDecimal transAmount, BigDecimal newBalance, BigDecimal freezeWithdraw) {
        UserAccount userAccount = new UserAccount();
        userAccount.setFuid(uid);
        userAccount.setFbalance(newBalance.longValue());
        userAccount.setFfreezeWithdraw(transAmount.add(freezeWithdraw).longValue());
        userAccount.setFoperateRemark("申请提现,金额:" + transAmount + "分");

        Result<UserAccount> result = userAccountApi.queryById(uid);
        if (!result.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        if (null == result.getData()) throw new BizException(ResultStatus.NOT_IMPLEMENTED);
        // 乐观锁-先查原来的值
        userAccount.setFmodifyTime(result.getData().getFmodifyTime());

        Result<Integer> accountResult = userAccountApi.updateNotNull(userAccount);
        if (!accountResult.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        if (accountResult.getData() < 0) throw new BizException(ResultStatus.NOT_IMPLEMENTED);
    }

    private void addAccountTransWater(String transId) {
        Result<UserAccountTrans> userAccountTransResult = userAccountTransApi.queryById(transId);
        if (!userAccountTransResult.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        if (null == userAccountTransResult.getData()) throw new BizException(ResultStatus.NOT_IMPLEMENTED);

        UserAccountTransWater accountTransWater = new UserAccountTransWater();
        BeanUtils.copyProperties(userAccountTransResult.getData(), accountTransWater);
        accountTransWater.setFtransId(transId);
        Result<Integer> accountWaterResult = userAccountTransWaterApi.create(accountTransWater);
        if (!accountWaterResult.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        if (accountWaterResult.getData() < 0) throw new BizException(ResultStatus.NOT_IMPLEMENTED);
    }

    private void addAccountTrans(UserAccountTrans userAccountTrans, String transId) {
        userAccountTrans.setFtransId(transId);

        Result<Integer> accountTransResult = userAccountTransApi.create(userAccountTrans);
        if (!accountTransResult.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        if (accountTransResult.getData() < 0) throw new BizException(ResultStatus.NOT_IMPLEMENTED);
    }

    private Long withdrawCheck(@Valid WithdrawDto withdrawDto) {
        Long uid = withdrawDto.getUid();

        if (!this.checkPayPwd(uid)) {

            throw new BizException(MallResultStatus.USER_PAY_PWD_NOT_SET);
        }

        BigDecimal transAmount = PriceUtil.toPenny(withdrawDto.getWithdrawAmount());
        withdrawDto.setWithdrawAmount(transAmount);

        if (StringUtil.isBlank(withdrawDto.getAccountNumber()) && StringUtil.isBlank(withdrawDto.getCardNumber())) {

            throw new BizException(MallResultStatus.WITHDRAW_ACCOUNT_EMPTY);
        }

        User user = this.checkUser(uid);

        WalletAmountVo walletAmount = this.queryAmount(uid);
        BigDecimal balance = PriceUtil.toPenny(walletAmount.getBalance());

        if (balance.compareTo(new BigDecimal("0.00")) <= 0 ||
                withdrawDto.getWithdrawAmount().compareTo(balance) > 0) {
            throw new BizException(MallResultStatus.ACCOUNT_BALANCE_INSUFFICIENT);
        }

        String passWord = EncryptUtils.aesDecrypt(withdrawDto.getWithdrawPwd());

        if (StringUtil.isBlank(passWord)) throw new BizException(MallResultStatus.WITHDRAW_PASSWORD_ERROR);

        passWord = MD5Util.MD5EncodeUtf8(passWord);

        if (!passWord.equals(user.getFwithdrawPasswd()))
            throw new BizException(MallResultStatus.WITHDRAW_PASSWORD_ERROR);

        Result<UserAccount> result = userAccountApi.queryById(uid);
        if (!result.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        if (null == result.getData()) throw new BizException(MallResultStatus.ACCOUNT_NOT_EXIST);

        if (result.getData().getFfreezeWithdraw() < 0) {
            log.warn("提现冻结金额小于0");
            throw new BizException(MallResultStatus.REEZE_WITHDRAW_ERROR);
        }

        BigDecimal sub = new BigDecimal(result.getData().getFbalance()).subtract(withdrawDto.getWithdrawAmount());
        if (sub.longValue() < 0L) {
            throw new BizException(MallResultStatus.ACCOUNT_BALANCE_INSUFFICIENT);
        }

        WithdrawRateVo withdrawRate = this.queryWithdrawRate(new WithdrawRateDto().setFwithdrawType(withdrawDto.getWay())).stream().findFirst().get();

        if (PriceUtil.toPenny(withdrawRate.getMinimumAmount()).compareTo(withdrawDto.getWithdrawAmount())>0){
            throw new BizException(MallResultStatus.WITHDRAW_LES_MIN_AMOUNT);
        }

        return uid;
    }


    private List<WithdrawRateVo> toWithdrawRateVos(List<UserWithdrawRate> userRate) {
        return userRate.stream().map(rate ->
                new WithdrawRateVo()
                        //取 除一百万乘100
                        .setFrate((new BigDecimal(rate.getFrate()).divide(ONE_MILLION)).multiply(HUNDRED).setScale(6, BigDecimal.ROUND_HALF_UP))
                        .setMinimumAmount(PriceUtil.toYuan(rate.getMinimumAmount()))
                        .setFwithdrawType(rate.getFwithdrawType())
        ).collect(Collectors.toList());
    }


    private User checkUser(Long uid) {
        Result<User> userResult = userApi.queryById(uid);
        if (!userResult.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);

        User user = userResult.getData();
        if (null == user) throw new BizException(MallResultStatus.NAME_NOT_EXIST);

        // 是否删除：0否，1是
        if (user.getFisDelete() == 1) throw new BizException(MallResultStatus.ACCOUNT_NOT_EXIST);

        // 冻结状态 ：1正常，2冻结
        if (user.getFfreezeStatus() != 1) throw new BizException(MallResultStatus.ACCOUNT_FREEZE);

        // 用户状态：1未认证，2 认证中，3 已认证，4未通过
        if (user.getFverifyStatus() != 3) throw new BizException(MallResultStatus.ACCOUNT_NOT_AUTH);

        // 手机号是否验证：0否，1是
        //if (user.getFmoblieIsValid() != 1) throw new BizException(MallResultStatus.ACCOUNT_MOBILE_NOT_VERIFY);

        // 邮箱是否验证：0否，1是
        //if (user.getFmailIsValid() != 1) throw new BizException(MallResultStatus.ACCOUNT_MAIL_NOT_VERIFY);
        return user;
    }

    @Getter
    private class WithdrawRate {
        private Integer way;
        private BigDecimal feeRate;

        public WithdrawRate(Integer way) {
            this.way = way;
        }

        public WithdrawRate get() {
            // 提现费率，若不存在则默认为0
            float poundagePercent = 0f;
            List<WithdrawRateVo> withdrawRates = WalletServiceImpl.this.queryWithdrawRate(new WithdrawRateDto().setFwithdrawType(way));
            if (!CollectionUtils.isEmpty(withdrawRates)) {
                poundagePercent = withdrawRates.stream().findFirst().get().getFrate().floatValue();
            }
            //计算手续费
            feeRate = (new BigDecimal(poundagePercent).divide(ONE_MILLION).setScale(6,BigDecimal.ROUND_HALF_UP));
            return this;
        }
    }

    @Getter
    private class AccountTrans {
        private @Valid WithdrawDto withdrawDto;
        private Long uid;
        private WithdrawRate withdrawRate;
        private BigDecimal transAmount;
        private UserAccountTrans userAccountTrans;
        private BigDecimal feeAmount;
        private BigDecimal transActualAmount;

        public AccountTrans(@Valid WithdrawDto withdrawDto, Long uid, WithdrawRate withdrawRate, BigDecimal transAmount) {
            this.withdrawDto = withdrawDto;
            this.uid = uid;
            this.withdrawRate = withdrawRate;
            this.transAmount = transAmount;
        }

        public AccountTrans build() {
            userAccountTrans = new UserAccountTrans();
            userAccountTrans.setFuid(uid);
            userAccountTrans.setFtransTypes(2);//提现
            userAccountTrans.setFtransReason("提现申请");
            userAccountTrans.setFtransAmount(transAmount.longValue());
            feeAmount = ((transAmount.multiply(withdrawRate.getFeeRate())).setScale(6,BigDecimal.ROUND_HALF_UP));
            transActualAmount = transAmount.subtract(feeAmount).setScale(6, BigDecimal.ROUND_HALF_UP);
            userAccountTrans.setFtransActualAmount(transActualAmount.longValue());
            userAccountTrans.setFtransPoundage(feeAmount.longValue());
            userAccountTrans.setFaccountHolder(withdrawDto.getName());
            userAccountTrans.setFtransMethod(2);

            // 支付宝
            if (withdrawDto.getWay() == 1) {
                userAccountTrans.setFrechargeType(1);
                userAccountTrans.setFtransStatus(2);
                userAccountTrans.setFwithdrawType(1);
                userAccountTrans.setFwithdrawAccount(withdrawDto.getAccountNumber());

            //银行卡
            }else if (withdrawDto.getWay() == 2){
                userAccountTrans.setFrechargeType(4);
                userAccountTrans.setFtransStatus(2);
                userAccountTrans.setFwithdrawType(2);
                userAccountTrans.setFwithdrawBank(withdrawDto.getBankName());
                userAccountTrans.setFwithdrawAccount(withdrawDto.getCardNumber());
            }

            return this;
        }
    }

    @Getter
    private class CheckAfterMoney {
        private Long uid;
        private BigDecimal transAmount;
        private BigDecimal newBalance;
        private BigDecimal freezeWithdraw;

        public CheckAfterMoney(Long uid, BigDecimal transAmount) {
            this.uid = uid;
            this.transAmount = transAmount;
        }

        public CheckAfterMoney check() {
            Result<UserAccount> resultAccount = userAccountApi.queryById(uid);
            if (!resultAccount.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);

            UserAccount account = resultAccount.getData();
            if (null == account) throw new BizException(MallResultStatus.ACCOUNT_NOT_EXIST);

            BigDecimal oldBalance = new BigDecimal(account.getFbalance());
            newBalance = oldBalance.subtract(transAmount);
            freezeWithdraw = new BigDecimal(account.getFfreezeWithdraw());
            if (oldBalance.longValue() <= 0L || transAmount.longValue() <= 0L || newBalance.longValue() < 0L || freezeWithdraw.longValue() < 0L) {
                // 回滚事务
                log.info("提现金额有误|提现失败|事务回滚");
                throw new BizException(ResultStatus.NOT_IMPLEMENTED);
            }
            return this;
        }
    }

    private String getWithdrawKey(WithdrawDto withdrawDto) {

        return StringUtils.join(Lists.newArrayList(MallRedisConstant.ADD_USER_WITHDRAW_LOCK, this.appendKey(withdrawDto), withdrawDto.getUid()), ":");
    }

    private String appendKey(WithdrawDto dto) {

        return Lists.newArrayList(dto.getName(), dto.getWay(), StringUtil.isBlank(dto.getAccountNumber()) ? "" : dto.getAccountNumber(), StringUtil.isBlank(dto.getCardNumber()) ? "" : dto.getCardNumber(), dto.getWithdrawAmount()).toString();
    }
}