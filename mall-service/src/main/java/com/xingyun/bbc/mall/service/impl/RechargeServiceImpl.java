package com.xingyun.bbc.mall.service.impl;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.user.api.UserAccountApi;
import com.xingyun.bbc.core.user.api.UserAccountTransApi;
import com.xingyun.bbc.core.user.api.UserAccountTransWaterApi;
import com.xingyun.bbc.core.user.api.UserAccountWaterApi;
import com.xingyun.bbc.core.user.api.UserDetailApi;
import com.xingyun.bbc.core.user.po.UserAccount;
import com.xingyun.bbc.core.user.po.UserAccountTrans;
import com.xingyun.bbc.core.user.po.UserAccountTransWater;
import com.xingyun.bbc.core.user.po.UserAccountWater;
import com.xingyun.bbc.core.user.po.UserDetail;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.enums.MallResultStatus;
import com.xingyun.bbc.mall.base.enums.XyUserAccountTrans.UserAccountTransThdPayTypeEnum;
import com.xingyun.bbc.mall.base.enums.XyUserDetail.UserDetailTypeEnum;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.common.enums.OrderPayMent.OrderPayEnum;
import com.xingyun.bbc.mall.service.RechargeService;
import com.xingyun.bbc.pay.model.vo.PayInfoVo;

import io.seata.spring.annotation.GlobalTransactional;



/** 
* @ClassName: RechargeServiceImpl 
* @Description: TODO
* @author Yangli
* @date 2018年12月11日 上午10:27:08 
*  
*/
@Service
public class RechargeServiceImpl implements RechargeService{
	
	private static Logger logger = LoggerFactory.getLogger(RechargeServiceImpl.class);

	@Autowired
	private  UserAccountTransApi  transApi;
	@Autowired
	private UserAccountApi accountApi;
	
	@Autowired
	private UserAccountWaterApi accountWaterApi;
	
	@Autowired
	private UserAccountTransWaterApi transWaterApi;
	
	@Autowired
	private UserDetailApi detailApi;
	
    @Autowired
    private DozerHolder dozerHolder;
	
    
	/**
	 * @author jianghui
	 * @version V1.0
	 * @Description: 第三方支付回调成功，写入充值到余额
	 * @date 2019/8/20 13:49
	 */
    @Override
	@GlobalTransactional
	public int newUpdateAfterRechargeSuccess(PayInfoVo thirdPayInfo) {
		int rechargeSuccessStatus = 3;//充值成功状态
		int thirdPayType = Integer.parseInt(thirdPayInfo.getThirdPayType());
		String forderId = thirdPayInfo.getForderId();
		long thirdPayAmount = Long.parseLong(thirdPayInfo.getPayAmount());
		String thirdTradeNo = thirdPayInfo.getThirdTradeNo();
		
		Result<UserAccountTrans> transInfoResult = transApi.queryById(forderId);
		if (!transInfoResult.isSuccess()) {
	            logger.info("查询用户充值单失败");
	            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
	    }
		UserAccountTrans transInfo=transInfoResult.getData();
		if (transInfo == null) {
			logger.info("充值记录不存在。订单号：" + forderId);
			return 0;
		}
		if (transInfo.getFtransStatus() == rechargeSuccessStatus) {
			logger.info("充值记录已成功。订单号：" + forderId);
			return 2;
		}
		if (thirdPayAmount!=transInfo.getFtransAmount()) {
			logger.info("充值失败，支付金额和充值单金额不匹配！！！。订单号：" + forderId + "充值单金额：" + transInfo.getFtransAmount() + "支付金额：" + thirdPayAmount);
			return 0;
		}
		transInfo.setFtransStatus(rechargeSuccessStatus);
		transInfo.setFrechargeType(this.thirdPayTypeToTransThirdPayType(thirdPayType));
		transInfo.setFtransThdUid(thirdTradeNo);
		transInfo.setFtransThdDetail(thirdPayInfo.getPayAccount());
		Date date=new Date();
		transInfo.setFpayTime(date);
		
		Result<Integer> flagTrans = transApi.updateNotNull(transInfo);
		
		if (!flagTrans.isSuccess()) {
            logger.info("更新用户充值单失败");
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
		}
		
		if (flagTrans.getData()==0) {
             throw new BizException(MallResultStatus.COMMON_UPDATE_FAIL);
        }
		
		UserAccountTransWater accountTransWater=  dozerHolder.convert(transInfo,UserAccountTransWater.class);
		
		Result<Integer> flagTransWater = transWaterApi.create(accountTransWater);
		
		if (!flagTransWater.isSuccess()) {
            logger.info("更新用户充值流水失败");
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
		}
		
		if(flagTransWater.getData()==0){
			logger.info("更新用户充值流水失败");
	        throw new BizException(MallResultStatus.COMMON_UPDATE_FAIL);
		}
		Long fuid = transInfo.getFuid();
		Result<UserAccount> accountResult = accountApi.queryById(fuid);
		if (!accountResult.isSuccess()) {
            logger.info("查询用户账户失败");
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
		}
		UserAccount account=accountResult.getData();
		long frecharge=account.getFrecharge();
		long balance = account.getFbalance();
		long creditBalance = account.getFcreditBalance();
		long newBalance = thirdPayAmount+balance;
		long newFrecharge =thirdPayAmount+frecharge;
		account.setFbalance(newBalance);
		account.setFrecharge(newFrecharge);
		String remark = "自动充值，金额(分)：" + thirdPayAmount;
		account.setFoperateRemark(remark);
		Result<Integer> flagAccount = accountApi.updateNotNull(account);
		if (!flagAccount.isSuccess()) {
            logger.info("更新用户账户表失败");
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
		}
		
		if(flagAccount.getData()==0){
			logger.info("更新用户账户表失败");
	        throw new BizException(MallResultStatus.COMMON_UPDATE_FAIL);
		}
		UserAccountWater userAccountWater=  dozerHolder.convert(account,UserAccountWater.class);
		
		Result<Integer> flagAccountWater = accountWaterApi.create(userAccountWater);
		if (!flagAccountWater.isSuccess()) {
            logger.info("更新用户账户流水表失败");
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
		}
		
		if(flagAccountWater.getData()==0){
			logger.info("更新用户账户流水表失败");
	        throw new BizException(MallResultStatus.COMMON_UPDATE_FAIL);
		}
		
		int detailType = this.thirdPayTypeToUserDetailType(thirdPayType);
		
		UserDetail detail=new  UserDetail();
		detail.setFdetailType(detailType);
		detail.setFuid(fuid);
		detail.setFtypeId(forderId);
		detail.setFincomeAmount(thirdPayAmount);
		//余额=充值后余额+提现冻结金额+支付冻结金额
		detail.setFbalance(newBalance+account.getFfreezeWithdraw()+account.getFfreezePay());
		detail.setFcreditBalance(creditBalance);
		detail.setFaccountDate(date);
		detail.setFremark(remark);
		Result<Integer> flagUserDetail = detailApi.create(detail);
		if (!flagUserDetail.isSuccess()) {
            logger.info("更新用户账户明细表失败");
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
		}
		
		if(flagUserDetail.getData()==0){
			logger.info("更新用户账户明细表失败");
	        throw new BizException(MallResultStatus.COMMON_UPDATE_FAIL);
		}
		logger.info("充值成功！订单号：" + forderId);
		return 1;
	}
    
	//第三方支付类型转换成t_bbc_user_account_trans表frecharge_type类型
	private int thirdPayTypeToTransThirdPayType(int thirdPayType){
		if (thirdPayType == OrderPayEnum.ALI_PAY.getValue()) {
			return UserAccountTransThdPayTypeEnum.ALIPAY_RECHARGE.getValue();
		} else if (thirdPayType == OrderPayEnum.WECHAT_PAY.getValue()) {
			return UserAccountTransThdPayTypeEnum.WEIXIN_RECHARGE.getValue();
		} 
		return -1;
	}
	//第三方支付类型转换成t_bbc_user_detail表fdetail_type类型
	private int thirdPayTypeToUserDetailType(int thirdPayType) {
		if (thirdPayType == OrderPayEnum.ALI_PAY.getValue()) {
			return UserDetailTypeEnum.ALI_RECHARGE.getValue();
		} else if (thirdPayType == OrderPayEnum.WECHAT_PAY.getValue()) {
			return UserDetailTypeEnum.WECHAT_RECHARGE.getValue();
		} 
		return -1;
	}

}
