package com.xingyun.bbc.mall.service.impl;


import com.xingyun.bbc.mall.base.utils.DecodeUtil;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.base.utils.EncryptUtils;
import com.xingyun.bbc.mall.base.utils.MD5Util;
import com.xingyun.bbc.mall.base.utils.PriceUtil;
import com.xingyun.bbc.mall.base.utils.ThirdPayUtil;
import com.xingyun.bbc.mall.base.utils.ThirdPayUtilFactory;
import com.xingyun.bbc.mall.base.utils.WeixinPayUtil;
import com.xingyun.bbc.mall.common.constans.PayConstants;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.dto.BalancePayDto;
import com.xingyun.bbc.mall.model.dto.RemittancetRechargeDto;
import com.xingyun.bbc.mall.model.vo.OrderResultVo;
import com.xingyun.bbc.mall.service.PayService;
import com.xingyun.bbc.mall.service.RechargeService;
import com.xingyun.bbc.order.api.OrderPayApi;
import com.xingyun.bbc.order.model.dto.order.PayDto;
import com.xingyun.bbc.order.model.vo.pay.BalancePayVo;
import com.xingyun.bbc.order.model.vo.pay.ThirdPayVo;
import com.xingyun.bbc.pay.api.PayChannelApi;
import com.xingyun.bbc.pay.model.dto.ThirdPayDto;
import com.xingyun.bbc.pay.model.dto.ThirdPayResponseDto;
import com.xingyun.bbc.pay.model.vo.PayInfoVo;

import io.seata.spring.annotation.GlobalTransactional;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;
import com.xingyun.bbc.core.order.api.OrderPaymentApi;
import com.xingyun.bbc.core.order.po.OrderPayment;
import com.xingyun.bbc.core.user.api.UserAccountApi;
import com.xingyun.bbc.core.user.api.UserAccountTransApi;
import com.xingyun.bbc.core.user.api.UserAccountTransWaterApi;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.user.po.UserAccount;
import com.xingyun.bbc.core.user.po.UserAccountTrans;
import com.xingyun.bbc.core.user.po.UserAccountTransWater;
import com.xingyun.bbc.core.utils.Result;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author jianghui
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
@Service
public class PayServiceImpl implements PayService {


	private static Logger logger = LoggerFactory.getLogger(PayServiceImpl.class);

	@Autowired
	private PayChannelApi payApi;
	
	@Autowired
	private OrderPaymentApi orderPaymentApi;
	
	@Autowired
	private OrderPayApi orderPayApi;
	
    @Autowired
    private UserApi userApi;
    
	@Autowired
	private UserAccountApi userAccountApi;
    
	@Autowired
	private UserAccountTransApi userAccountTransApi;
	
	@Autowired
	private UserAccountTransWaterApi userAccountTransWaterApi;
    
    @Autowired
    private DozerHolder dozerHolder;
    @Autowired
    private ThirdPayUtilFactory thirdPayUtilFactory;
    
    @Autowired
    private RechargeService rechargeService;
	
	
	@Override
	public Result<?> balancePay(BalancePayDto dto, HttpServletRequest request) {
		
		String fuid= request.getHeader("xyid").toString();

		logger.info("余额支付。用户id：" +fuid+ "，订单号：" + dto.getForderId() + "，余额类型：" + dto.getBalanceType());
		
		Result<?> checkEntity = this.checkBalancePayParams(fuid, dto.getForderId(), dto.getPayPwd(), Integer.parseInt(dto.getBalanceType()));
		if (checkEntity != null) {
			return checkEntity;
		}
		// 检查订单是否能支付
		checkEntity = this.checkOrderIsEnablePay(dto);
		if (checkEntity != null) {
			return checkEntity;
		}
		/////////////////
		Long totalAmount=null;//订单总金额
		Long unPayAmount=null;//未支付金额
		OrderPayment orderPayment=orderPaymentApi.queryById(dto.getForderId()).getData();
		
		//查询账号余额信息
		UserAccount account=userAccountApi.queryById(fuid).getData();
		if(account==null)
		{
			logger.info("余额支付。用户id：" +fuid+ "账号信息不存在");
			return Result.failure(MallExceptionCode.USER_FREEZE_ERROR);
		}else{
			if(account.getFbalance()==0)
			{
				logger.info("余额支付。用户id：" +fuid+ "余额为0!");
				return Result.failure(MallExceptionCode.BALANCE_NOT_ENOUGH);
			}
			if(account.getFfreezeWithdraw()>0)
			{
				logger.info("余额支付。用户id：" +fuid+ "提现冻结金额:"+account.getFfreezeWithdraw());
				return Result.failure(MallExceptionCode.FREEZE_WITHDRAW);
			}
		}
		totalAmount= orderPayment.getFtotalOrderAmount();
		unPayAmount = totalAmount - orderPayment.getFbalancePayAmount() - orderPayment.getFcreditPayAmount();
		Long fbalance=account.getFbalance();

		
		PayDto payDto =new PayDto();
		payDto.setForderPaymentId(dto.getForderId());
		Result<BalancePayVo> code = orderPayApi.balancePay(payDto);
		
		OrderResultVo orderResultVo=new OrderResultVo();
		//余额不足支付 此时为混合支付
		if(unPayAmount > fbalance){
			orderResultVo.setOrder_status(1); //还需要第三方支付状态
		}else{
			orderResultVo.setOrder_status(2); //余额足够
		}
		
		if(code.getData().getCode()==200)
		{
			logger.info("余额部分支付成功。订单:"+dto.getForderId()+",总金额:"+PriceUtil.toYuan(totalAmount)+",金额："+fbalance);	
			orderResultVo.setCode(200);
			orderResultVo.setMsg("余额支付成功");
			return Result.success(orderResultVo);
		}else{
			logger.info("余额部分支付失败。订单:"+dto.getForderId()+",总金额:"+PriceUtil.toYuan(totalAmount)+",金额："+fbalance);	
			orderResultVo.setCode(code.getData().getCode());
			orderResultVo.setMsg("余额支付失败");
			return Result.success(orderResultVo);
		}

	}
	
	

	@Override
	public Result<?> createThirdPayUrl(ThirdPayDto dto,HttpServletRequest request) {
		String appVersion= request.getHeader("appVersion");
		Result<?> result=this.checkThirdPayUrlParams(dto,request);
		if(result!=null)
		{
			return result;
		}
		logger.info("充值参数，订单号：" + dto.getForderId()+"订单金额："+dto.getPayAmount());
		dto.setAppVersion(appVersion);
		return payApi.createThirdPayUrl(dto);
	}
	
	@Override
	public Result<?> createThirdPayCode(ThirdPayDto dto,HttpServletRequest request) {
	
		Result<?> result=this.checkThirdPayUrlParams(dto, request);
		if(result!=null)
		{
			return result;
		}
		logger.info("充值参数，订单号：" + dto.getForderId()+"订单金额："+dto.getPayAmount());
		return payApi.createThirdPayCode(dto);
	}
	
	@Override
	public Result<?> newThirdPayResponse(@PathVariable String urlSuffix, HttpServletRequest request, HttpServletResponse response) {	
		logger.info("第三方支付回调url："+request.getRequestURL()+"?"+request.getQueryString());
		logger.info("第三方支付回调参数：" + JSON.toJSONString(request.getParameterMap()));
		String returnUrlSuffix = "return", notifyUrlSuffix = "notify";
		if (!returnUrlSuffix.equals(urlSuffix) && !notifyUrlSuffix.equals(urlSuffix)) {
			logger.info("第三方支付响应URL错误。url后缀：" + urlSuffix);
			return Result.failure(MallExceptionCode.THIRD_PAY_NOTIFY_FAIL);
		}
		Map<String, String> extraParams = this.parseExtraParams(request);
		if (extraParams == null) {
			logger.info("第三方支付回调异常，扩展参数为空。");
			logger.info(JSON.toJSONString(request.getParameterMap()));
			return Result.failure(MallExceptionCode.THIRD_PAY_NOTIFY_FAIL);
		}
		String payType = extraParams.get("payType");
		String payScene = extraParams.get("payScene");
		String isTerminal = extraParams.get("isTerminal");
	
		
		ThirdPayUtil thirdPayUtil = "1".equals(isTerminal) ? thirdPayUtilFactory.createTerminalPayUtil(payType) : thirdPayUtilFactory.createThirdPayUtil(payType);
//		if (thirdPayUtil == null) {
//			return Result.failure(MallExceptionCode.THIRD_PAY_NOTIFY_FAIL);
//		}
//		Map<String, String> thirdPayInfo = thirdPayUtil.modifyParseInfoFromThirdPayResponse(request, response);
		
		Map<String, String> params=new HashMap<String, String>();
		
		ThirdPayResponseDto thirdPayResponseDto=new ThirdPayResponseDto();
		thirdPayResponseDto.setPayType(payType);
		thirdPayResponseDto.setIsTerminal(isTerminal);
		thirdPayResponseDto.setParams(params);
		PayInfoVo thirdPayInfo= (PayInfoVo) payApi.thirdPayResponse(thirdPayResponseDto).getData();
		
		if (thirdPayInfo == null) {
			return Result.failure(MallExceptionCode.THIRD_PAY_NOTIFY_FAIL);
		}
		logger.info("------------------第三方支付回调返回："+thirdPayInfo);
		String forderId = thirdPayInfo.getForderId();
		int flag = 0;
		if (ThirdPayUtil.PAY_SCENE_RECHARGE.equals(payScene)) {//充值
			flag = rechargeService.newUpdateAfterRechargeSuccess(thirdPayInfo);
			if (flag > 0) {
//				this.closeOtherThirdPayOrder(forderId, payType);
//				if (returnUrlSuffix.equals(urlSuffix)) {
//					logger.info("支付订单:" + thirdPayInfo.get("forderId") + "充值返回" + thirdPayConfig.getRecharge_success_request());
//					return Result.ok(thirdPayConfig.getRecharge_success_request());
//				}
				//logger.info("支付订单:" + thirdPayInfo.get("forderId") + "充值返回" + thirdPayUtil.thirdPayNotifySuccess(response));
				//return Result.success(thirdPayUtil.thirdPayNotifySuccess(response));
				logger.info("------------------第三方支付回调充值返回成功："+thirdPayInfo);
				return Result.success();
			}else{
				logger.info("------------------第三方支付回调充值返回失败："+thirdPayInfo);
			}
		} else 
			if (ThirdPayUtil.PAY_SCENE_ORDER.equals(payScene)) { //订单
			PayDto payDto=new PayDto();
			payDto.setForderPaymentId(forderId);
			payDto.setForderThirdpayType(Integer.valueOf(thirdPayInfo.getThirdPayType()));
			payDto.setPayAmount(Long.parseLong(thirdPayInfo.getPayAmount()));
			payDto.setPayTime(thirdPayInfo.getPayTime());
			payDto.setThirdTradeNo(thirdPayInfo.getThirdTradeNo());
			payDto.setPayAccount(thirdPayInfo.getPayAccount());
			payDto.setPayName(thirdPayInfo.getPayName());
			logger.info("------------------第三方支付回调请求订单中心参数：forderId="+payDto.getForderPaymentId()+",forderThirdpayType="+payDto.getForderThirdpayType()
				+",payAmount="+payDto.getPayAmount()+",payTime="+payDto.getPayName()+",thirdTradeNo="+payDto.getThirdTradeNo()+",payAccount="+payDto.getPayAccount()
				+",payName="+payDto.getPayName());
			Result<ThirdPayVo> result=orderPayApi.thirdPay(payDto);
			logger.info("------------------第三方支付回调请求订单中心返回："+result.getData().getCode());
			return result;
		}
		return Result.failure(MallExceptionCode.THIRD_PAY_NOTIFY_FAIL);
	
	}
	
	
	@Override
	public Result<?> thirdPayResponse(@PathVariable String urlSuffix, HttpServletRequest request, HttpServletResponse response) {	
		logger.info("第三方支付回调url："+request.getRequestURL()+"?"+request.getQueryString());
		logger.info("第三方支付回调参数：" + JSON.toJSONString(request.getParameterMap()));
		String returnUrlSuffix = "return", notifyUrlSuffix = "notify";
		if (!returnUrlSuffix.equals(urlSuffix) && !notifyUrlSuffix.equals(urlSuffix)) {
			logger.info("第三方支付响应URL错误。url后缀：" + urlSuffix);
			return Result.failure(MallExceptionCode.THIRD_PAY_NOTIFY_FAIL);
		}
		Map<String, String> extraParams = this.parseExtraParams(request);
		if (extraParams == null) {
			logger.info("第三方支付回调异常，扩展参数为空。");
			logger.info(JSON.toJSONString(request.getParameterMap()));
			return Result.failure(MallExceptionCode.THIRD_PAY_NOTIFY_FAIL);
		}
		String payType = extraParams.get("payType");
		String payScene = extraParams.get("payScene");
		String isTerminal = extraParams.get("isTerminal");
		ThirdPayUtil thirdPayUtil = "1".equals(isTerminal) ? thirdPayUtilFactory.createTerminalPayUtil(payType) : thirdPayUtilFactory.createThirdPayUtil(payType);
		if (thirdPayUtil == null) {
			return Result.failure(MallExceptionCode.THIRD_PAY_NOTIFY_FAIL);
		}
		Map<String, String> thirdPayInfo = thirdPayUtil.modifyParseInfoFromThirdPayResponse(request, response);
		if (thirdPayInfo == null) {
			return Result.failure(MallExceptionCode.THIRD_PAY_NOTIFY_FAIL);
		}
		logger.info("------------------第三方支付回调返回："+thirdPayInfo);
		String forderId = thirdPayInfo.get("forderId");
		int flag = 0;
		if (ThirdPayUtil.PAY_SCENE_RECHARGE.equals(payScene)) {//充值
			flag = rechargeService.updateAfterRechargeSuccess(thirdPayInfo);
			if (flag > 0) {
//				this.closeOtherThirdPayOrder(forderId, payType);
//				if (returnUrlSuffix.equals(urlSuffix)) {
//					logger.info("支付订单:" + thirdPayInfo.get("forderId") + "充值返回" + thirdPayConfig.getRecharge_success_request());
//					return Result.ok(thirdPayConfig.getRecharge_success_request());
//				}
				//logger.info("支付订单:" + thirdPayInfo.get("forderId") + "充值返回" + thirdPayUtil.thirdPayNotifySuccess(response));
				//return Result.success(thirdPayUtil.thirdPayNotifySuccess(response));
				logger.info("------------------第三方支付回调充值返回成功："+thirdPayInfo);
				return Result.success();
			}else{
				logger.info("------------------第三方支付回调充值返回失败："+thirdPayInfo);
			}
		} else 
			if (ThirdPayUtil.PAY_SCENE_ORDER.equals(payScene)) { //订单
			PayDto payDto=new PayDto();
			payDto.setForderPaymentId(forderId);
			payDto.setForderThirdpayType(Integer.valueOf(thirdPayInfo.get("thirdPayType")));
			payDto.setPayAmount(Long.parseLong(PriceUtil.toPenny(thirdPayInfo.get("payAmount")).toString()));
			payDto.setPayTime(thirdPayInfo.get("payTime"));
			payDto.setThirdTradeNo(thirdPayInfo.get("thirdTradeNo"));
			payDto.setPayAccount(thirdPayInfo.get("payAccount"));
			payDto.setPayName(thirdPayInfo.get("payName"));
			logger.info("------------------第三方支付回调请求订单中心参数：forderId="+payDto.getForderPaymentId()+",forderThirdpayType="+payDto.getForderThirdpayType()
				+",payAmount="+payDto.getPayAmount()+",payTime="+payDto.getPayName()+",thirdTradeNo="+payDto.getThirdTradeNo()+",payAccount="+payDto.getPayAccount()
				+",payName="+payDto.getPayName());
			Result<ThirdPayVo> result=orderPayApi.thirdPay(payDto);
			logger.info("------------------第三方支付回调请求订单中心返回："+result.getData().getCode());
			return result;
		}
		return Result.failure(MallExceptionCode.THIRD_PAY_NOTIFY_FAIL);
	}
	
	
	@Override
	@GlobalTransactional
	public Result<?> addBalance(RemittancetRechargeDto dto) {
		
		UserAccountTrans userAccountTrans=new UserAccountTrans();
		userAccountTrans.setFtransId(dto.getForderId());
		userAccountTrans.setFtransTypes(Integer.valueOf(dto.getPayType()));
		userAccountTrans.setFpayVoucher(dto.getPayVoucher());
		Result<Integer> result=userAccountTransApi.updateNotNull(userAccountTrans);
			if(result.isSuccess())
			{
				UserAccountTrans water=userAccountTransApi.queryById(dto.getForderId()).getData();
				UserAccountTransWater transWater = dozerHolder.convert(water,UserAccountTransWater.class);
				userAccountTransWaterApi.updateNotNull(transWater);
			}else{
				return Result.failure(MallExceptionCode.REMITTANCE_PAY_FAIL);
			}
		return Result.success(result);
	}
	
	
	// 校验输入的支付密码
	private Result<?> checkBalancePayParams(String fuid, String forderId, String inputPayPwdEncrypt, int balanceType) {
		// 检查fuid是否为空
		if (Strings.isNullOrEmpty(fuid) || fuid.trim().length() == 0) {
			logger.info("校验余额支付失败：用户名为空！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType);
			return Result.failure(MallExceptionCode.PARAM_ERROR);
		}
		// 检查订单号是否为空
		if (Strings.isNullOrEmpty(forderId) || forderId.trim().length() == 0) {
			logger.info("校验余额支付失败：订单号为空！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType);
			return Result.failure(MallExceptionCode.PARAM_ERROR);
		}

		// 检查余额类型
		if (balanceType != 1 && balanceType != 2) {
			logger.info("校验余额支付失败：余额类型错误！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType);
			return Result.failure(MallExceptionCode.PARAM_ERROR);
		}
	
			// 检查加密支付密码是否为空
			if (Strings.isNullOrEmpty(inputPayPwdEncrypt) || inputPayPwdEncrypt.trim().length() == 0) {
				logger.info("校验余额支付失败：加密支付密码为空！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType);
				return Result.failure(MallExceptionCode.PARAM_ERROR);
			}
			String inputPayPwd = EncryptUtils.aesDecrypt(inputPayPwdEncrypt); // 支付密码解码
			// APP解密
			if (Strings.isNullOrEmpty(inputPayPwd)) {
				inputPayPwd = DecodeUtil.decodeApp(inputPayPwdEncrypt);
			}
			// 判断输入的支付密码是否能解码
			if (Strings.isNullOrEmpty(inputPayPwd) || inputPayPwd.trim().length() == 0) {
				logger.info("校验余额支付失败：支付密码解码失败！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType + "，加密支付密码：" + inputPayPwdEncrypt);
				return Result.failure(MallExceptionCode.WITHDRAW_PSD_WRONG);
			}
			// 查询设置的支付密码和MD5(inputPayPwd)
			Result<User> userResult = userApi.queryById(fuid);
			if (userResult == null) {
				logger.info("校验余额支付失败：找不到该用户记录！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType);
				return Result.failure(MallExceptionCode.ORDER_NOT_EXIST);
			}
			String payPwdMd5 = userResult.getData().getFwithdrawPasswd();
			if (Strings.isNullOrEmpty(payPwdMd5) || payPwdMd5.trim().length() == 0) {
				logger.info("校验余额支付失败：未设置支付密码！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType);
				return Result.failure(MallExceptionCode.PAY_PWD_IS_NOT_SET);
			}
			if (!payPwdMd5.equals(MD5Util.MD5EncodeUtf8(inputPayPwd))) {
				logger.info("校验余额支付失败：支付密码错误！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType);
				return Result.failure(MallExceptionCode.WITHDRAW_PSD_WRONG);
			}

		return null;
	}

	// 校验支付参数
	public Result<?> checkThirdPayUrlParams(ThirdPayDto dto,HttpServletRequest request) {
		logger.info("生成第三方支付链接。订单号:" + dto.getForderId() + ",支付场景:" + dto.getPayScene()+ ",支付类型:" + dto.getPayType());
		// 检查订单或者充值是否能支付。
		if (PayConstants.PAY_SCENE_RECHARGE.equals(dto.getPayScene())) { //充值
			Result<?> checkEntity = this.checkRechargeIsEnable(request,dto);
			if (checkEntity != null) {
				return checkEntity;
			}
		} else 
		if(PayConstants.PAY_SCENE_ORDER.equals(dto.getPayScene()) ){//订单
			Result<?> checkEntity = this.checkOrderIsEnablePay(dto);
			if (checkEntity != null) {
				return checkEntity;
			}
		} 
		return null;
	}
	
	//校验是否可以充值
	public Result<?> checkRechargeIsEnable(HttpServletRequest request,ThirdPayDto dto) {
		UserAccountTrans userAccountTrans = userAccountTransApi.queryById(dto.getForderId()).getData();
		// 充值记录不存在或者已完成
		if (userAccountTrans == null || userAccountTrans.getFtransStatus()!= 1) {
			logger.info("充值订单不存在或已支付。");
			return Result.failure(MallExceptionCode.ORDER_NOT_EXIST);
		}
		// String和Integer直接比较不会为true
//		String fuid = request.getHeader("xyid");
//		if (!fuid.equals(String.valueOf(userAccountTrans.getFuid()))) {
//			logger.info("充值订单用户id和订单用户不匹配");
//			return Result.failure(MallExceptionCode.ORDER_NOT_MATCHING);
//		}
		dto.setPayAmount(PriceUtil.toYuan(userAccountTrans.getFtransAmount()).toString());
		return null;
	}
	
	// 校验订单是否能支付
	private Result<?> checkOrderIsEnablePay(ThirdPayDto dto) {
		Date lockTime = null; // 订单超时时间
		//String orderFuid = null; // 订单用户
		Long orderTotalAmount = null; // 订单总金额
		Long fbalancePayAmount = null; // 订单已付金额
		int orderStatus = -1; // 订单状态
		int thirdTradeStatus = -1; // 第三方支付状态
		//Date orderCreateTime = null; // 订单超时时间，有些第三方支付需要
		OrderPayment orderPayment = orderPaymentApi.queryById(dto.getForderId()).getData();
			if (orderPayment == null) {
				logger.info("支付订单号查询返回结果为空！ 订单号：" + dto.getForderId());
				return Result.failure(MallExceptionCode.ORDER_NOT_EXIST);
			}
			lockTime = orderPayment.getFlockTime();
			//orderFuid = String.valueOf(orderPayment.getFuid());
			orderTotalAmount = orderPayment.getFtotalOrderAmount();
			fbalancePayAmount=orderPayment.getFbalancePayAmount();
			orderStatus = orderPayment.getForderStatus();
			thirdTradeStatus = orderPayment.getFthirdTradeStatus();
			//orderCreateTime = orderPayment.getFcreateTime();
		
//		if (!fuid.equals(orderFuid)) {
//			logger.info("订单与用户不匹配！用户：" + fuid + "，订单号：" + forderId + "，订单fuid：" + orderFuid);
//			return Result.failure(MallExceptionCode.ORDER_NOT_MATCHING);
//		}
//		if (lockTime.compareTo(new Date()) < 0) {
//			logger.info("订单已过期！用户："  + "，订单号：" + dto.getForderId());
//			return Result.failure(MallExceptionCode.ORDER_AS_CANCELLED);
//		}
		if (orderStatus == 5 || thirdTradeStatus == 2) {
			logger.info("订单已完成！用户："  + "，订单号：" + dto.getForderId());
			return Result.failure(MallExceptionCode.ORDER_AS_CANCELLED);
		}
		if (orderStatus == 3) {
			logger.info("订单已取消！用户："  + "，订单号：" + dto.getForderId());
			return Result.failure(MallExceptionCode.ORDER_IS_COMPLETION);
		}
		
		Long payAmouts=orderTotalAmount-fbalancePayAmount;
		dto.setLockTime(lockTime);
		dto.setPayAmount(PriceUtil.toYuan(payAmouts).toString());
		dto.setRecieveName(orderPayment.getFpayerName());
		return null;
	}
	
	
		// 从交易回调中获取扩展参数
		private Map<String, String> parseExtraParams(HttpServletRequest request) {
			// 支付宝扩展参数
			Map<String, String> extraParams = (Map<String, String>) JSON.parseObject(request.getParameter("extra_common_param"), new TypeReference<HashMap<String, String>>() { });
			if(extraParams == null) {
				// 支付宝Open平台二维码扩展参数, 因为是存在body字段中,怕重复.
				extraParams = JSON.parseObject(request.getParameter("body"), new TypeReference<HashMap<String, String>>() { });
				if(extraParams == null || !ThirdPayUtilFactory.PAY_TYPE_ALI_OPEN.equals(extraParams.get("payType"))) {
					extraParams = null;
				}
			}
			if (extraParams == null) {
				// 微信扩展参数
				String inputLine;
				StringBuffer notityXml = new StringBuffer();
				Map<String, String> params = new HashMap<>();
				try {
					while ((inputLine = request.getReader().readLine()) != null) {
						notityXml.append(inputLine);
					}
				} catch (IOException e) {
					logger.info("p_notityXml:"+notityXml.toString());
				}	
				params = WeixinPayUtil.getMapFromXML(notityXml.toString());
				logger.info("微信支付回调解析前字符串：" + notityXml.toString());
				try {
					request.getReader().close();
				} catch (IOException e) {
				}
				if (params != null) {
					logger.info("微信支付回调参数：" + JSON.toJSONString(params));
					request.setAttribute("notifyParams", params);
					request.setAttribute("notityXml", notityXml);
					extraParams = JSON.parseObject(params.get("attach"), new TypeReference<HashMap<String, String>>() { });
				}
			}
			return extraParams;
		}

		
		public static String readReqStr(HttpServletRequest request){
	        BufferedReader reader = null;
	        StringBuilder sb = new StringBuilder();
	        try{
	            reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
	            String line = null;
	            while ((line = reader.readLine()) != null){
	                sb.append(line);
	            }
	        } catch (IOException e){
	            e.printStackTrace();
	        } finally {
	            try{
	                if (null != reader){ reader.close();}
	            } catch (IOException e){
	            	e.printStackTrace();
	            }
	        }
	        return sb.toString();
	    }

}
