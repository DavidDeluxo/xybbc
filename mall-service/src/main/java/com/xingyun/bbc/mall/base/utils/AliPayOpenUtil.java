package com.xingyun.bbc.mall.base.utils;


import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayRequest;
import com.alipay.api.AlipayResponse;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeCancelModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.xingyun.bbc.mall.base.config.AlipayOpenConfig;
import com.xingyun.bbc.mall.base.config.ThirdPayConfig;
import com.xingyun.bbc.mall.common.enums.OrderPayMent.OrderPayEnum;

import com.xingyun.bbc.pay.model.dto.ThirdPayDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

/**
 * 支付宝-开放平台
 * @author Tito
 *
 */
@Component
public class AliPayOpenUtil implements ThirdPayUtil{
	
	private static Logger logger = LoggerFactory.getLogger(AliPayOpenUtil.class);
	
	private final static String ALI_WAIT_BUYER_PAY = "WAIT_BUYER_PAY"; // 待支付
	private static final String ALI_TRADE_CLOSED = "TRADE_CLOSED"; // 交易关闭或取消
	private static final String ALI_TRADE_SUCCESS = "TRADE_SUCCESS"; // 交易成功
	private static final String ALI_TRADE_FINISHED = "TRADE_FINISHED"; // 交易完成
	
	public static final String NOTIFY_SUCCESS = "success";
	
	@Autowired
	private ThirdPayConfig thirdPayConfig;

	@Autowired
	private AlipayOpenConfig alipayConfig;

	@Override
	public Map<String, String> createThirdPayUrl(ThirdPayDto thirdPayDto) {
		String urlType = ThirdPayUtil.URL_TYPE_QR_CODE;
		String payAmount = thirdPayDto.getPayAmount(); // 支付金额
		Object lockTime = thirdPayDto.getLockTime().getTime();
		AlipayClient aliClient = this.initAliClient();
		AlipayTradePrecreateRequest aliRequest = new AlipayTradePrecreateRequest(); // 生成二维码,不支持弹窗
		aliRequest.setApiVersion(alipayConfig.getVersion());
		aliRequest.setNotifyUrl(thirdPayConfig.getPay_notify_request()); // 异步回调
		AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
		model.setOutTradeNo(thirdPayDto.getForderId()); // 订单号
		model.setSubject(thirdPayDto.getForderId()); // 支付标题
		model.setTotalAmount(payAmount); // 支付金额
		// 支付过期时间
		if (lockTime != null && ThirdPayUtil.PAY_SCENE_ORDER.equals(thirdPayDto.getPayScene())) {
			long timeOut = ((long) lockTime - (new Date()).getTime()) / (1000 * 60); // 计算支付过期时间
			model.setTimeoutExpress(timeOut + "m");
		}
		// 需要回传的参数, 支付宝二维码不支持自定义扩展参数,只能写到body中
		model.setBody(ThirdPayUtil.jointExtendParams(ThirdPayUtilFactory.PAY_TYPE_ALI_OPEN, thirdPayDto.getPayScene(), null));
		aliRequest.setBizModel(model);
		AlipayTradePrecreateResponse response = null;
		try {
			response = aliClient.execute(aliRequest);
		} catch (Exception e) {
			String errorMsg = "支付宝生成二维码异常";
			logger.error(errorMsg);
			logger.error(e.getMessage());
			return ThirdPayUtil.fillThirdPayUrlInfo(urlType, null, errorMsg);
		}
		if(response == null) {
			String errorMsg = "支付宝生成二维码异常";
			logger.error(errorMsg);
			return ThirdPayUtil.fillThirdPayUrlInfo(urlType, null, errorMsg);
		}

		String thirdPayUrl = response.getQrCode();
		boolean isSuccess = response.isSuccess();

		if (!isSuccess) {
			logger.info("支付宝Open平台生成二维码失败! 网关代码: {}, 网关信息: {}, 业务代码: {}, 业务信息: {}", response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
			return ThirdPayUtil.fillThirdPayUrlInfo(urlType, null, response.getSubMsg());
		}

		return ThirdPayUtil.fillThirdPayUrlInfo(urlType, thirdPayUrl, null);
	}


	@Override
	public Map<String, String> modifyParseInfoFromThirdPayResponse(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> params = ParameteMapUtil.getParameters(request);


		boolean isVerify = this.verify(params, response);
		if (!isVerify) {
			return null;
		}
		String forderId = request.getParameter("out_trade_no");
		String payAmount = request.getParameter("total_amount");
		String thirdTradeNo = request.getParameter("trade_no"); // 支付宝交易号
		String payTime = request.getParameter("notify_time");
		String payAccount = request.getParameter("buyer_logon_id");
		String payName = request.getParameter("buyer_id");
		String recieveAccount = request.getParameter("seller_email");
		String recieveName = request.getParameter("seller_id");
		String thirdPayType = String.valueOf(OrderPayEnum.ALI_PAY.getValue());
		return ThirdPayUtil.fillThirdPayInfo(forderId, thirdPayType, payAmount, payTime, thirdTradeNo, null, null, payAccount, payName, recieveAccount, recieveName);
	}

	@Override
	public String thirdPayNotifySuccess(HttpServletResponse response) {
		return NOTIFY_SUCCESS;
	}

	/**
	 * AlipayTradeCloseRequest 只能在用户扫了二维码但没支付的情况下生效. 生成二维码但没扫过, 会提示订单不存在.
	 * AlipayTradeCancelResponse 支付成功后再取消退款，使用要慎重！
	 * @param forderId
	 * @return
	 */
	@Override
	public String closeThirdPayOrder(String forderId) {
		logger.info("支付宝关闭二维码，订单号：" + forderId);
		AlipayRequest<AlipayTradeCloseResponse> aliRequest = new AlipayTradeCloseRequest();
		aliRequest.setApiVersion(alipayConfig.getVersion());
		AlipayTradeCancelModel model = new AlipayTradeCancelModel();
		model.setOutTradeNo(forderId);
		aliRequest.setBizModel(model);
		AlipayResponse response = this.executeAlipayResponse(aliRequest);
		if (response == null) {
			String errorMsg = "订单: " + forderId + "关闭支付宝二维码失败! 支付宝Open平台服务器异常...";
			logger.info(errorMsg);
			return errorMsg;
		}
		boolean isSuccess = response.isSuccess();
		
		if (!isSuccess) {
			logger.info("订单: {}支付宝Open平台关闭订单失败! 网关代码: {}, 网关信息: {}, 业务代码: {}, 业务信息: {}", forderId, response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
			return response.getSubMsg();
		}
		return null;
	}
	
	private AlipayClient initAliClient() {
		String serverUrl = alipayConfig.getOpenapi_url();
		String appId = alipayConfig.getApp_id();
		String privateKey = alipayConfig.getPrivate_key();
		String format = alipayConfig.getFormat();
		String charset = alipayConfig.getCharset();
		String publicKey = alipayConfig.getPublic_key();
		String signType = alipayConfig.getSign_type();
		return new DefaultAlipayClient(serverUrl, appId, privateKey, format, charset, publicKey, signType);
	}
	
	private AlipayResponse executeAlipayResponse(AlipayRequest<?> aliRequest) {
		AlipayClient aliClient = this.initAliClient();
		AlipayResponse response = null;
		try {
			response = aliClient.execute(aliRequest);
		} catch (Exception e) {
			String errorMsg = "支付宝Open平台响应异常";
			logger.error(errorMsg);
			logger.error(e.getMessage());
		}
		return response;
	}
	
	private boolean verify(Map<String, String> params, HttpServletResponse response) {
		boolean signVerified = false;
		try {
			signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getPublic_key(), alipayConfig.getCharset(), alipayConfig.getSign_type());
		} catch (AlipayApiException e) {
			logger.info("支付宝Open平台, 回调验签异常!!!");
			return false;
		}
		if (!signVerified) {
			logger.info("支付宝Open平台, 回调验签失败!!!");
			return false;
		}
		String tradeStatus = params.get("trade_status");
		logger.info("支付宝Open平台回调状态: " + tradeStatus);
		if(ALI_WAIT_BUYER_PAY.equals(tradeStatus)) {
			this.replySuccessInResponse(response);  // 这个只是告诉支付宝收到了该异步通知, 并不会影响支付状态
			return false;
		}
		if (ALI_TRADE_SUCCESS.equals(tradeStatus) || ALI_TRADE_FINISHED.equals(tradeStatus)) {
			
			return true;
		}
		return false;
	}
	
	private void replySuccessInResponse(HttpServletResponse response) {
		ServletOutputStream out = null;
		try {
			out = response.getOutputStream();
			out.print(NOTIFY_SUCCESS);
			out.flush();
		} catch (IOException e) {
			logger.error("支付宝Open平台回调返回信息异常: 读取输出流报错!");
			logger.error(e.getMessage());
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("支付宝Open平台回调返回信息异常: 关闭输出流报错!");
					logger.error(e.getMessage());
				}
			}
		}
	}
	
	/**
	 * 支付宝Open平台立即下单接口, 适合跳页面, 该接口使用二维码会比较麻烦.
	 * @param request
	 * @param forderId
	 * @param payType
	 * @param payScene
	 * @param bankCode
	 * @param payChannel
	 * @return
	 */
	@SuppressWarnings("unused")
	private Map<String, String> webPay(HttpServletRequest request, String forderId, String payType, String payScene, String bankCode, String payChannel) {
		String payAmount = (String) request.getAttribute("payAmount"); // 支付金额
		String urlType = ThirdPayUtil.URL_TYPE_NEW_WINDOW;
		AlipayClient aliClient = this.initAliClient();
		AlipayTradePagePayRequest aliRequest = new AlipayTradePagePayRequest();
		aliRequest.setApiVersion(alipayConfig.getVersion());
		aliRequest.setNotifyUrl(thirdPayConfig.getPay_notify_request()); // 异步回调
		if(ThirdPayUtil.URL_TYPE_NEW_WINDOW.equals(urlType)) {
			aliRequest.setReturnUrl(thirdPayConfig.getPay_return_request()); // 立即响应
		}
		AlipayTradePagePayModel payModel = new AlipayTradePagePayModel();
		payModel.setProductCode("FAST_INSTANT_TRADE_PAY");
		payModel.setOutTradeNo(forderId); // 订单号
		payModel.setSubject("title"); // 支付标题
		payModel.setTotalAmount(payAmount); // 支付金额
		payModel.setBody("content"); // 支付内容
		try {
			// 需要回传的参数
			payModel.setPassbackParams(URLEncoder.encode(ThirdPayUtil.jointExtendParams(ThirdPayUtilFactory.PAY_TYPE_ALI_OPEN, payScene, null), alipayConfig.getCharset()));  
		} catch (UnsupportedEncodingException e) {
			String errorMsg = "支付宝扩展参数UrlEncode失败";
			logger.error(errorMsg);
			logger.error(e.getMessage());
			return ThirdPayUtil.fillThirdPayUrlInfo(urlType, null, errorMsg); 
		}
		payModel.setQrPayMode("2");
		aliRequest.setBizModel(payModel);
		AlipayTradePagePayResponse response = null;
		try {
			response = aliClient.pageExecute(aliRequest);
		} catch (Exception e) {
			String errorMsg = "支付宝验签错误";
			logger.error(errorMsg);
			logger.error(e.getMessage());
			return ThirdPayUtil.fillThirdPayUrlInfo(urlType, null, errorMsg);
		}
		if (response == null || !response.isSuccess()) {
			logger.info("支付宝开放平台验签失败! 网关代码: {}, 网关信息: {}, 业务代码: {}, 业务信息: {}", response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
			return ThirdPayUtil.fillThirdPayUrlInfo(urlType, null, response.getSubMsg());
		}
		return ThirdPayUtil.fillThirdPayUrlInfo(urlType, response.getBody(), null);
	}


}
