package com.xingyun.bbc.mallpc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;
import com.xingyun.bbc.core.operate.api.OrderConfigApi;
import com.xingyun.bbc.core.operate.po.OrderConfig;
import com.xingyun.bbc.core.order.api.OrderPaymentApi;
import com.xingyun.bbc.core.order.po.OrderPayment;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.user.api.UserAccountApi;
import com.xingyun.bbc.core.user.api.UserAccountTransApi;
import com.xingyun.bbc.core.user.api.UserAccountTransWaterApi;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.user.po.UserAccount;
import com.xingyun.bbc.core.user.po.UserAccountTrans;
import com.xingyun.bbc.core.user.po.UserAccountTransWater;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.constants.PayConstants;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.DecodeUtil;
import com.xingyun.bbc.mallpc.common.utils.EncryptUtils;
import com.xingyun.bbc.mallpc.common.utils.Md5Utils;
import com.xingyun.bbc.mallpc.common.utils.PriceUtil;
import com.xingyun.bbc.mallpc.common.utils.TimeAddUtil;
import com.xingyun.bbc.mallpc.service.PayService;
import com.xingyun.bbc.order.api.OrderPayApi;
import com.xingyun.bbc.order.model.dto.order.PayDto;
import com.xingyun.bbc.order.model.vo.pay.BalancePayVo;
import com.xingyun.bbc.order.model.vo.pay.ThirdPayVo;
import com.xingyun.bbc.pay.api.PayChannelApi;
import com.xingyun.bbc.pay.model.dto.ThirdPayDto;
import com.xingyun.bbc.pay.model.dto.ThirdPayResponseDto;
import com.xingyun.bbc.pay.model.vo.PayInfoVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jianghui
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
@Service
public class PayServiceImpl implements PayService {

	private static Logger logger = LoggerFactory.getLogger(PayServiceImpl.class);

	private static final Long DEFAULT_LOCK_TIME = 2l * 60l;

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
	private OrderConfigApi orderConfigApi;

	@Autowired
	private DozerHolder dozerHolder;

	/**
	 * @author jianghui
	 * @version V1.0
	 * @Description: 生成第三方支付链接
	 * @date 2019/8/20 13:49
	 */
	@Override
	public Result<?> createThirdPayUrl(ThirdPayDto dto, HttpServletRequest request) {
		String appVersion = request.getHeader("appVersion");
		Result<?> result = this.checkThirdPayUrlParams(dto, request);
		if (result != null) {
			return result;
		}
		logger.info("充值参数，订单号：" + dto.getForderId() + "订单金额：" + dto.getPayAmount());
		dto.setAppVersion(appVersion);
		return payApi.createThirdPayUrl(dto);
	}

	/**
	 * @author jianghui
	 * @version V1.0
	 * @Description: 生成第三方支付二维码
	 * @date 2019/8/20 13:49
	 */
	@Override
	public Result<?> createThirdPayCode(ThirdPayDto dto, HttpServletRequest request) {

		Result<?> result = this.checkThirdPayUrlParams(dto, request);
		if (result != null) {
			return result;
		}
		logger.info("充值参数，订单号：" + dto.getForderId() + "订单金额：" + dto.getPayAmount());
		return payApi.createThirdPayCode(dto);
	}

	
	// 校验输入的支付密码
	private Result<?> checkBalancePayParams(String fuid, String forderId, String inputPayPwdEncrypt, int balanceType) {
		// 检查fuid是否为空
		if (Strings.isNullOrEmpty(fuid) || fuid.trim().length() == 0) {
			logger.info("校验余额支付失败：用户名为空！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType);
			return Result.failure(MallPcExceptionCode.PARAM_ERROR);
		}
		// 检查订单号是否为空
		if (Strings.isNullOrEmpty(forderId) || forderId.trim().length() == 0) {
			logger.info("校验余额支付失败：订单号为空！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType);
			return Result.failure(MallPcExceptionCode.PARAM_ERROR);
		}

		// 检查余额类型
		if (balanceType != 1 && balanceType != 2) {
			logger.info("校验余额支付失败：余额类型错误！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType);
			return Result.failure(MallPcExceptionCode.PARAM_ERROR);
		}

		// 检查加密支付密码是否为空
		if (Strings.isNullOrEmpty(inputPayPwdEncrypt) || inputPayPwdEncrypt.trim().length() == 0) {
			logger.info("校验余额支付失败：加密支付密码为空！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType);
			return Result.failure(MallPcExceptionCode.PARAM_ERROR);
		}
		String inputPayPwd = EncryptUtils.aesDecrypt(inputPayPwdEncrypt); // 支付密码解码
		// pc解密
		if (Strings.isNullOrEmpty(inputPayPwd)) {
			inputPayPwd = DecodeUtil.decodeWeb(inputPayPwdEncrypt);
		}
		// 判断输入的支付密码是否能解码
		if (Strings.isNullOrEmpty(inputPayPwd) || inputPayPwd.trim().length() == 0) {
			logger.info("校验余额支付失败：支付密码解码失败！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType + "，加密支付密码："
					+ inputPayPwdEncrypt);
			return Result.failure(MallPcExceptionCode.WITHDRAW_PSD_WRONG);
		}
		// 查询设置的支付密码和MD5(inputPayPwd)
		Result<User> userResult = userApi.queryById(fuid);
		if (userResult == null) {
			logger.info("校验余额支付失败：找不到该用户记录！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType);
			return Result.failure(MallPcExceptionCode.ORDER_NOT_EXIST);
		}
		String payPwdMd5 = userResult.getData().getFwithdrawPasswd();
		if (Strings.isNullOrEmpty(payPwdMd5) || payPwdMd5.trim().length() == 0) {
			logger.info("校验余额支付失败：未设置支付密码！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType);
			return Result.failure(MallPcExceptionCode.PAY_PWD_IS_NOT_SET);
		}
		if (!payPwdMd5.equals(Md5Utils.toMd5(inputPayPwd))) {
			logger.info("校验余额支付失败：支付密码错误！订单号：" + forderId + "，用户名：" + fuid + "，余额类型：" + balanceType);
			return Result.failure(MallPcExceptionCode.WITHDRAW_PSD_WRONG);
		}

		return null;
	}

	// 校验支付参数
	public Result<?> checkThirdPayUrlParams(ThirdPayDto dto, HttpServletRequest request) {
		logger.info("生成第三方支付链接。订单号:" + dto.getForderId() + ",支付场景:" + dto.getPayScene() + ",支付类型:" + dto.getPayType());
		// 检查订单或者充值是否能支付。
		if (PayConstants.PAY_SCENE_RECHARGE.equals(dto.getPayScene())) { // 充值
			Result<?> checkEntity = this.checkRechargeIsEnable(request, dto);
			if (checkEntity != null) {
				return checkEntity;
			}
		} else if (PayConstants.PAY_SCENE_ORDER.equals(dto.getPayScene())) {// 订单
			Result<?> checkEntity = this.checkOrderIsEnablePay(dto);
			if (checkEntity != null) {
				return checkEntity;
			}
		}
		return null;
	}

	// 校验是否可以充值
	public Result<?> checkRechargeIsEnable(HttpServletRequest request, ThirdPayDto dto) {
		UserAccountTrans userAccountTrans = userAccountTransApi.queryById(dto.getForderId()).getData();
		// 充值记录不存在或者已完成
		if (userAccountTrans == null || userAccountTrans.getFtransStatus() != 1) {
			logger.info("充值订单不存在或已支付。");
			return Result.failure(MallPcExceptionCode.ORDER_NOT_EXIST);
		}
		// String和Integer直接比较不会为true
//		String fuid = request.getHeader("xyid");
//		if (!fuid.equals(String.valueOf(userAccountTrans.getFuid()))) {
//			logger.info("充值订单用户id和订单用户不匹配");
//			return Result.failure(MallPcExceptionCode.ORDER_NOT_MATCHING);
//		}
		dto.setPayAmount(PriceUtil.toYuan(userAccountTrans.getFtransAmount()).toString());
		return null;
	}

	// 校验订单是否能支付
	private Result<?> checkOrderIsEnablePay(ThirdPayDto dto) {
		Date lockTime = null; // 订单超时时间
		Long orderTotalAmount = null; // 订单总金额
		Long fbalancePayAmount = null; // 订单已付金额
		int orderStatus = -1; // 订单状态
		int thirdTradeStatus = -1; // 第三方支付状态
		OrderPayment orderPayment = orderPaymentApi.queryById(dto.getForderId()).getData();
		if (orderPayment == null) {
			logger.info("支付订单号查询返回结果为空！ 订单号：" + dto.getForderId());
			return Result.failure(MallPcExceptionCode.ORDER_NOT_EXIST);
		}

		Long fminute = 0l;

		Criteria<OrderConfig, Object> orderConfigCriteria = Criteria.of(OrderConfig.class);
		orderConfigCriteria.andEqualTo(OrderConfig::getForderConfigType, 0);// 0待支付订单限时支付
		orderConfigCriteria.andEqualTo(OrderConfig::getFstatus, 0);
		orderConfigCriteria.fields(OrderConfig::getForderConfigType, OrderConfig::getFminute);
		Result<List<OrderConfig>> orderConfigResult = orderConfigApi.queryByCriteria(orderConfigCriteria);
		if (!orderConfigResult.isSuccess()) {
			fminute = DEFAULT_LOCK_TIME;
		} else {
			List<OrderConfig> orderConfigList = orderConfigResult.getData();
			if (orderConfigList.size() > 0) {
				fminute = orderConfigList.get(0).getFminute();
			} else {
				fminute = DEFAULT_LOCK_TIME;
			}
		}

		lockTime = TimeAddUtil.addMinute(orderPayment.getFcreateTime(), fminute.intValue());
		orderTotalAmount = orderPayment.getFtotalOrderAmount();
		fbalancePayAmount = orderPayment.getFbalancePayAmount();
		orderStatus = orderPayment.getForderStatus();
		thirdTradeStatus = orderPayment.getFthirdTradeStatus();

		if (lockTime.compareTo(new Date()) < 0) {
			logger.info("订单已过期！" + "订单号：" + dto.getForderId());
			return Result.failure(MallPcExceptionCode.ORDER_IS_OVERDUE);
		}
		if (orderStatus == 5 || thirdTradeStatus == 2) {
			logger.info("订单已完成！" + "订单号：" + dto.getForderId());
			return Result.failure(MallPcExceptionCode.ORDER_AS_CANCELLED);
		}
		if (orderStatus == 3) {
			logger.info("订单已取消！" + "订单号：" + dto.getForderId());
			return Result.failure(MallPcExceptionCode.ORDER_IS_COMPLETION);
		}

		Long payAmouts = orderTotalAmount - fbalancePayAmount;
		dto.setLockTime(lockTime);
		dto.setPayAmount(PriceUtil.toYuan(payAmouts).toString());
		dto.setRecieveName(orderPayment.getFpayerName());
		return null;
	}

}
