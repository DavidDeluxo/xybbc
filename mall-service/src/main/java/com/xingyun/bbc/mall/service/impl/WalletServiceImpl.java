package com.xingyun.bbc.mall.service.impl;

import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.order.api.OrderPaymentApi;
import com.xingyun.bbc.core.order.po.OrderPayment;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.user.api.BankDepositApi;
import com.xingyun.bbc.core.user.api.UserAccountApi;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.api.UserWithdrawRateApi;
import com.xingyun.bbc.core.user.po.BankDeposit;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.user.po.UserAccount;
import com.xingyun.bbc.core.user.po.UserWithdrawRate;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.core.utils.StringUtil;
import com.xingyun.bbc.mall.base.enums.MallResultStatus;
import com.xingyun.bbc.mall.base.utils.PriceUtil;
import com.xingyun.bbc.mall.model.dto.WithdrawDto;
import com.xingyun.bbc.mall.model.dto.WithdrawRateDto;
import com.xingyun.bbc.mall.model.vo.BanksVo;
import com.xingyun.bbc.mall.model.vo.WalletAmountVo;
import com.xingyun.bbc.mall.model.vo.WithdrawRateVo;
import com.xingyun.bbc.mall.service.WalletService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
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
        return StringUtil.isBlank(user.getFwithdrawPasswd()) ? false : true;
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
        // 提现费率，若不存在则默认为0
//        Float poundagePercent = 0f;
//        this.queryWithdrawRate(new WithdrawRateDto().setFwithdrawType(withdrawDto.getWay()));
//        if(redisUtil.getFeeRate("withdraw") != null){
//
//
//            poundagePercent = Float.parseFloat(redisUtil.getFeeRate("withdraw"));
//        }
//        //计算手续费
//        BigDecimal feeRate = new BigDecimal(poundagePercent).divide(new BigDecimal(10000));
//        BigDecimal transAmount = new BigDecimal(userAccountTrans.getTransAmountParam());
//        userAccountTrans.setFtransAmount(transAmount.longValue());
//        BigDecimal feeAmount = transAmount.multiply(feeRate);
//        BigDecimal transActualAmount = transAmount.subtract(feeAmount);
//        userAccountTrans.setFtransActualAmount(transActualAmount.longValue());
//        userAccountTrans.setFtransPoundage(feeAmount.longValue());
//        int fuid = userAccountTrans.getFuid();
//        //判断金额是否正确（提现、余额金额不能小于等于0，更新后的余额、冻结金额不能小于0）
//        UserAccount account = userAccountMapper.queryAccount(fuid);
//        BigDecimal oldBalance = new BigDecimal(account.getFbalance());
//        BigDecimal newBalance = oldBalance.subtract(transAmount);
//        BigDecimal freezeWithdraw = new BigDecimal(account.getFfreezeWithdraw());
//        if(oldBalance.longValue() <= 0L || transAmount.longValue() <= 0L || newBalance.longValue() < 0L || freezeWithdraw.longValue() < 0L){
//            // 回滚事务
//            logger.info("提现金额有误，提现失败，事务回滚");
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//            return -1;
//        }
//        // 生成订单号
//        String ftransDetail = OrderIdCreator.getOrderId("T", serverId, userAccountTrans.getFuid());
//        // 提现申请插入数据库
//        userAccountTrans.setFtransDetail(ftransDetail);
//        int flag1 = userAccountTransMapper.insert(userAccountTrans);
//        logger.info("生成提现订单，用户id：" + userAccountTrans.getFuid() + ",订单号：" + ftransDetail);
//        logger.info("用户id：" + userAccountTrans.getFuid() + ",提现金额：" + PriceUtil.cartPennyToYuan(transAmount) + ",手续费：" + PriceUtil.cartPennyToYuan(feeAmount));
//        // 申请数据插入流水表
//        int flag2 = userAccountTransMapper.addWater(userAccountTrans.getFtransDetail());
//        // 修改用户账户表，冻结提现金额
//        UserAccount userAccount = new UserAccount();
//        userAccount.setFuid(fuid);
//        userAccount.setFbalance(newBalance.longValue());
//        userAccount.setFfreezeWithdraw(transAmount.add(freezeWithdraw).longValue());
//        userAccount.setFopMemo("申请提现，金额：" + transAmount + "分");
//        int flag3 = userAccountMapper.updateByFuid(userAccount);
//        logger.info("冻结提现金额：" + PriceUtil.cartPennyToYuan(transAmount) + "，用户id：" + userAccountTrans.getFuid());
//        int flag4 = userAccountMapper.insertWater(fuid);
//        if (flag1 > 0 && flag2 > 0 && flag3 > 0 && flag4 > 0) {
//            return 1;
//        }else{
//            logger.info("提现流程有误，事务回滚");
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//            return 0;
//        }
        return true;
    }

    private List<WithdrawRateVo> toWithdrawRateVos(List<UserWithdrawRate> userRate) {
        return userRate.stream().map(rate ->
                new WithdrawRateVo()
                        .setFrate(PriceUtil.toYuan(rate.getFrate()))
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
        if (user.getFmoblieIsValid() != 1) throw new BizException(MallResultStatus.ACCOUNT_MOBILE_NOT_VERIFY);

        // 邮箱是否验证：0否，1是
        if (user.getFmailIsValid() != 1) throw new BizException(MallResultStatus.ACCOUNT_MAIL_NOT_VERIFY);
        return user;
    }
}
