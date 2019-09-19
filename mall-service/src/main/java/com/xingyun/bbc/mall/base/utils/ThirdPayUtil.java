package com.xingyun.bbc.mall.base.utils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.xingyun.bbc.pay.model.dto.ThirdPayDto;

/** 
* @ClassName: ThirdPayUtil 
* @Description: 第三方支付接口
* @author Yangli
* @date 2018年12月11日 上午10:27:08
*  
*/
public interface ThirdPayUtil {

	// 支付场景
	public static final String PAY_SCENE_RECHARGE = "1"; // 充值
	public static final String PAY_SCENE_ORDER = "2"; // 订单
	
	// 生成的URL类型
	public static final String URL_TYPE_NEW_WINDOW = "1";// 新窗口
	public static final String URL_TYPE_QR_CODE = "2";// 二维码

	public static final String THIRD_PAY_NOTIFY_FAIL = "FAIL"; // 返回回调处理失败字符串

	/**
	* @Title: createThirdPayUrl 
	* @Description: 生成第三方支付链接 
	* @param forderId	订单号
	* @param payType	支付方式。和ThirdPayUtilFactory对应。支付宝和微信由thirdPay.aliPayWay和thirdPay.weixinPayWay决定
	* @param payScene	支付场景。1-充值，2-订单
	* @param bankCode	银行编码。
	* @param payChannel	支付通道。用来区分储蓄卡和信用卡等。
	* @return 第三方支付URL
	* @author Tito
	 */
	Map<String, String> createThirdPayUrl(ThirdPayDto thirdPayDto);

	/**
	* @Title: parseInfoFromThirdPayResponse 
	* @Description: 从第三方回调中解析出银行编码、支付金额等第三方支付信息。 
	* @param request
	* @param response
	* @return 第三方支付信息
	* @author Tito
	 */
	Map<String, String> modifyParseInfoFromThirdPayResponse(HttpServletRequest request, HttpServletResponse response);
	
	/**
	* @Title: thirdPayNotifySuccess 
	* @Description: 第三方支付异步回调返回“成功”字符串 
	* @return 成功字符串
	* @author Tito
	 */
	String thirdPayNotifySuccess(HttpServletResponse response);

	/**
	 * 关闭支付二维码接口，注意有些第三方支付可能会发生退款
	 * 不允许退款
	 * @param forderId
	 * @return
	 */
	String closeThirdPayOrder(String forderId);
	
	
	
	/**
	* @Title: fillThirdPayUrlInfo 
	* @Description: 填充第三方支付链接信息 
	* @param urlType url类型。1-新窗口，2-二维码
	* @param url 第三方支付链接
	* @return
	* @author Tito
	 */
	public static Map<String, String> fillThirdPayUrlInfo(String urlType, String url, String errorMsg) {
		Map<String, String> urlInfo = new HashMap<>();
		urlInfo.put("urlType", urlType);
		urlInfo.put("url", url);
		if (!Strings.isNullOrEmpty(errorMsg)) {
			urlInfo.put("errorMsg", errorMsg);
		}
		return urlInfo;
	}
	
	/**
	* @Title: jointExtendParams 
	* @Description: 拼接支付回调的扩展参数 
	* @param payType 支付类型
	* @param payScene 支付场景
	* @return
	* @author Tito
	 */
	public static String jointExtendParams(String payType, String payScene, String appVersion){
		if (appVersion == null) {
			return "{\"payType\":\"" + payType + "\",\"payScene\":\"" + payScene + "\"}";
		} else {
			return "{\"payType\":\"" + payType + "\",\"payScene\":\"" + payScene + "\",\"isTerminal\":\"" + 1 + "\"}";
		}
		
	}
	
	/**
	* @Title: fillThirdPayInfo 
	* @Description: 填充第三方支付信息 
	* @param forderId		订单号
	* @param thirdPayType	第三方支付类型
	* @param payAmount		支付金额(元)
	* @param payTime		支付时间
	* @param thirdTradeNo	第三方交易流水
	* @param bankCode		银行编码
	* @param bankTradeNO	银行流水
	* @param payAccount		付款账号
	* @param payName		付款姓名
	* @param recieveAccount	收款账号
	* @param recieveName	收款姓名
	* @return
	* @author Tito
	 */
	public static Map<String, String> fillThirdPayInfo(String forderId, String thirdPayType, String payAmount, String payTime, String thirdTradeNo, String bankCode, String bankTradeNO, String payAccount, String payName, String recieveAccount, String recieveName) {
		Map<String, String> payInfo = new HashMap<>();
		payInfo.put("forderId", forderId);
		payInfo.put("thirdPayType", thirdPayType);
		payInfo.put("payAmount", payAmount);
		payInfo.put("payTime", payTime);
		payInfo.put("thirdTradeNo", thirdTradeNo);
		payInfo.put("bankCode", bankCode);
		payInfo.put("bankTradeNO", bankTradeNO);
		payInfo.put("payAccount", payAccount);
		payInfo.put("payName", payName);
		payInfo.put("recieveAccount", recieveAccount);
		payInfo.put("recieveName", recieveName);
		return payInfo;
	}
}
