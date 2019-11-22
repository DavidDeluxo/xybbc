package com.xingyun.bbc.mallpc.service.impl;

import com.xingyun.bbc.common.redis.XyIdGenerator;
import com.xingyun.bbc.common.redis.order.OrderTypeEnum;
import com.xingyun.bbc.common.redis.order.RechargeOrderBizEnum;
import com.xingyun.bbc.core.user.api.UserAccountTransApi;
import com.xingyun.bbc.core.user.api.UserAccountTransWaterApi;
import com.xingyun.bbc.core.user.enums.AccountRechargeType;
import com.xingyun.bbc.core.user.enums.AccountTransType;
import com.xingyun.bbc.core.user.enums.UserAccountTransTypesEnum;
import com.xingyun.bbc.core.user.enums.UserTransMethod;
import com.xingyun.bbc.core.user.po.UserAccountTrans;
import com.xingyun.bbc.core.user.po.UserAccountTransWater;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.constants.MallPcConstants;
import com.xingyun.bbc.mallpc.common.ensure.EnsureHelper;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.RequestHolder;
import com.xingyun.bbc.mallpc.model.dto.recharge.OfflineRechargeVoucherDTO;
import com.xingyun.bbc.mallpc.model.dto.recharge.RechargeSubmitDTO;
import com.xingyun.bbc.mallpc.service.RechargeService;
import com.xingyun.bbc.pay.api.PayChannelApi;
import com.xingyun.bbc.pay.model.dto.ThirdPayDto;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author pengaoluo
 * @version 1.0.0
 */
@Slf4j
@Service
public class RechargeServiceImpl implements RechargeService {

    @Resource
    private DozerHolder dozerHolder;

    @Resource
    private UserAccountTransApi userAccountTransApi;

    @Resource
    private UserAccountTransWaterApi userAccountTransWaterApi;

    @Resource
    private PayChannelApi payApi;

    @GlobalTransactional
    @Override
    public String save(RechargeSubmitDTO dto) {
        log.debug("调用生成充值单方法,dto:{}", dto);
        Long fuid = RequestHolder.getUserId();
        //生成充值单号
        String ftransId = XyIdGenerator.generateId(OrderTypeEnum.RECHARGE_ORDER.getCode(), RechargeOrderBizEnum.TRADE.getCode());

        UserAccountTrans userAccountTrans = dozerHolder.convert(dto, UserAccountTrans.class);
        userAccountTrans.setFtransId(ftransId);
        userAccountTrans.setFtransTypes(UserAccountTransTypesEnum.RECHARGE.getCode());
        userAccountTrans.setFtransStatus(AccountTransType.WaitPayment.getCode());
        userAccountTrans.setFuid(fuid);
        userAccountTrans.setFaid(fuid);
        userAccountTrans.setFtransMethod(UserTransMethod.INCREASE.getCode());
        EnsureHelper.checkNotNullAndGetData(userAccountTransApi.create(userAccountTrans));
        UserAccountTransWater userAccountTransWater = dozerHolder.convert(userAccountTrans, UserAccountTransWater.class);
        EnsureHelper.checkSuccess(userAccountTransWaterApi.create(userAccountTransWater));
        log.debug("生成充值单,userAccountTrans:{}", userAccountTrans);
        return ftransId;
    }

    @Override
    public void offlineVoucher(OfflineRechargeVoucherDTO dto) {
        log.debug("调用线下汇款提交凭证方法,dto:{}", dto);
        String ftransId = dto.getFtransId();

        UserAccountTrans userAccountTrans = EnsureHelper.checkNotNullAndGetData(userAccountTransApi.queryById(ftransId), MallPcExceptionCode.RECORD_NOT_EXIST);
        checkUserAccountTransOfOffline(userAccountTrans);
        //为了防止覆盖更新时间,重新创建对象用于更新
        UserAccountTrans userAccountTransForUpdate = new UserAccountTrans();
        userAccountTransForUpdate.setFtransId(ftransId);
        userAccountTransForUpdate.setFtransStatus(AccountTransType.WaitVerify.getCode());
        userAccountTransForUpdate.setFpayVoucher(dto.getFpayVoucher());
        EnsureHelper.checkSuccess(userAccountTransApi.updateNotNull(userAccountTransForUpdate));
        log.debug("线下汇款提交凭证成功,userAccountTransForUpdate:{}", userAccountTransForUpdate);
    }

    /**
     * 校验线下汇款记录
     *
     * @param userAccountTrans
     */
    private void checkUserAccountTransOfOffline(UserAccountTrans userAccountTrans) {
        Assert.isTrue(UserAccountTransTypesEnum.RECHARGE.getCode().equals(userAccountTrans.getFtransTypes()), "该交易记录并非充值类型");
        Assert.isTrue(AccountTransType.WaitPayment.getCode().equals(userAccountTrans.getFtransStatus()), "该交易记录并非待付款状态");
        Assert.isTrue(AccountRechargeType.OfflinePay.getCode().equals(userAccountTrans.getFrechargeType()), "该交易记录并非线下汇款");
    }

    @Override
    public Object getQRCode(String ftransId) {
        UserAccountTrans userAccountTrans = EnsureHelper.checkNotNullAndGetData(userAccountTransApi.queryById(ftransId), MallPcExceptionCode.RECORD_NOT_EXIST);
        checkUserAccountTransOfQRCode(userAccountTrans);
        ThirdPayDto thirdPayDto = new ThirdPayDto();
        //有效时间一分钟
        thirdPayDto.setLockTime(new Date(System.currentTimeMillis() + MallPcConstants.ONE_MINITE_OF_MILLI));
        thirdPayDto.setPayAmount(userAccountTrans.getFtransAmount().toString());
        thirdPayDto.setForderId(ftransId);
        thirdPayDto.setPayScene("1");
        thirdPayDto.setPayType(AccountRechargeType.AliPay.getCode().equals(userAccountTrans.getFrechargeType()) ? "1" : "2");
        return EnsureHelper.checkNotNullAndGetData(payApi.createThirdPayCode(thirdPayDto));
    }

    /**
     * 校验付款码充值记录
     *
     * @param userAccountTrans
     */
    private void checkUserAccountTransOfQRCode(UserAccountTrans userAccountTrans) {
        Assert.isTrue(UserAccountTransTypesEnum.RECHARGE.getCode().equals(userAccountTrans.getFtransTypes()), "该交易记录并非充值类型");
        Assert.isTrue(AccountTransType.WaitPayment.getCode().equals(userAccountTrans.getFtransStatus()), "该交易记录并非待付款状态");
        Assert.isTrue(AccountRechargeType.AliPay.getCode().equals(userAccountTrans.getFrechargeType()) || AccountRechargeType.WechatPay.getCode().equals(userAccountTrans.getFrechargeType())
                , "该交易记录并非付款码充值类型");
    }

}
