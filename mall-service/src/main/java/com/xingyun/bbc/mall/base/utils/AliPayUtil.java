package com.xingyun.bbc.mall.base.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.xingyun.bbc.pay.model.dto.ThirdPayDto;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import com.xingyun.bbc.mall.base.config.ThirdPayConfig;
import com.xingyun.bbc.mall.common.enums.OrderPayMent.OrderPayEnum;
import com.xingyun.bbc.mall.base.config.AlipayOpenConfig;;

/** 
* @ClassName: AlipayUtil 
* @Description: TODO
* @author Yangli
* @date 2018年12月11日 上午10:27:08
*  
*/
@Component
public class AliPayUtil implements ThirdPayUtil{
	
	private static Logger logger = LoggerFactory.getLogger(AliPayUtil.class);
	
	private static final String REPLY_NOTIFY_SUCCESS = "success"; // 支付回调返回成功
	private static final String SIGN_TYPE_MD5 = "MD5"; // 验签方式
	@Autowired
	private AlipayConfig aliPayConfig;
	@Autowired
	private ThirdPayConfig thirdPayConfig;


	@Override
	public Map<String, String> createThirdPayUrl(ThirdPayDto thirdPayDto) {
		String appVersion = thirdPayDto.getAppVersion(); 
		String payAmount = thirdPayDto.getPayAmount();
		Date lockTime = thirdPayDto.getLockTime();
		Map<String, String> params = new HashMap<String, String>();
		params.put("notify_url", thirdPayConfig.getPay_notify_request()); // 异步回调url
		params.put("return_url", thirdPayConfig.getPay_return_request()); // 立即响应url
		params.put("out_trade_no", thirdPayDto.getForderId()); // 外部订单号
		params.put("subject", thirdPayDto.getForderId()); // 支付标题
		params.put("total_fee", payAmount); // 支付金额
		params.put("body", thirdPayDto.getForderId()); // 支付内容
		params.put("extra_common_param", ThirdPayUtil.jointExtendParams(ThirdPayUtilFactory.PAY_TYPE_ALI, thirdPayDto.getPayScene(), appVersion)); // 其它参数
		if (lockTime != null) {
			long timeOut = (lockTime.getTime() - (new Date()).getTime()) / (1000 * 60); // 计算支付过期时间
			params.put("it_b_pay", timeOut + "m"); // 支付过期时间
		}
		params.put("partner", aliPayConfig.getPartner());
		params.put("seller_email", aliPayConfig.getSeller_email());
		params.put("_input_charset", aliPayConfig.getInput_charset());
		params.put("payment_type", aliPayConfig.getPayment_type());
		String signType = aliPayConfig.getSign_type();
		if (appVersion == null) {
			params.put("service", aliPayConfig.getService());
		} else {
			params.put("service", aliPayConfig.getService_app());
		}
			
		this.filterParams(params);
		String linkedString = this.jointLinkString(params);
		String url = "";
		if (appVersion == null) {
			String sign = this.getSign(linkedString);
			linkedString = linkedString + "&sign=" + sign + "&sign_type=" + signType;
			url = aliPayConfig.getReq_url() + linkedString;
		} else {
			params.remove("seller_email");
			params.put("return_url", aliPayConfig.getReturn_url());
			params.put("code", "APPALIPAY");
			params.put("seller_id", aliPayConfig.getSeller_email());
			linkedString = this.jointLinkString(params);
			String appSign = "";
			appSign = this.getSign(linkedString);
			url = linkedString + "&sign=" + appSign + "&sign_type=" + signType;
		}
		return ThirdPayUtil.fillThirdPayUrlInfo(ThirdPayUtil.URL_TYPE_NEW_WINDOW, url, null);
	}


	@Override
	public Map<String, String> modifyParseInfoFromThirdPayResponse(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> params = this.getNotifyParams(request);
		boolean isVerify = this.verify(params);

		if (!isVerify) {
			return null;
		}
		String forderId = request.getParameter("out_trade_no");
		String payAmount = request.getParameter("total_fee");
		String thirdTradeNo = request.getParameter("trade_no");
		String payTime = request.getParameter("notify_time");
		String payAccount = request.getParameter("buyer_email");
		String payName = request.getParameter("buyer_id");
		String recieveAccount = request.getParameter("seller_email");
		String recieveName = request.getParameter("seller_id");
		String thirdPayType = String.valueOf(OrderPayEnum.ALI_PAY.getValue());
		return ThirdPayUtil.fillThirdPayInfo(forderId, thirdPayType, payAmount, payTime, thirdTradeNo, null, null, payAccount, payName, recieveAccount, recieveName);
	}
	
	@Override
	public String thirdPayNotifySuccess(HttpServletResponse response){
		return REPLY_NOTIFY_SUCCESS;
	}
	
	/**
	* @Title: filterParams 
	* @Description: 去掉空值和前面参数 
	* @param params
	* @author Tito
	 */
	private void filterParams(Map<String, String> params) {
		if (params == null || params.size() == 0) {
			return;
		}
		Map<String, String> tempMap = new HashMap<>();
		for (String key : params.keySet()) {
			String value = params.get(key);
			if (Strings.isNullOrEmpty(value) || "sign".equalsIgnoreCase(key) || "sign_type".equalsIgnoreCase(key)) {
				continue;
			}
			tempMap.put(key, value);
		}
		params.clear();
		params.putAll(tempMap);
	}
	
	/**
	* @Title: jointLinkString 
	* @Description: 拼接字符串 
	* @param params
	* @return
	* @author Tito
	 */
	private String jointLinkString(Map<String, String> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		StringBuffer buf = new StringBuffer();
		for (int i = 0, len = keys.size(); i < len; i++) {
			String key = keys.get(i);
			String value = params.get(key);
			if (i > 0) {
				buf.append("&");
			}
			buf.append(key).append("=").append(value);
		}
		return String.valueOf(buf);
	}
	
	private String getSign(String data) {
		if (SIGN_TYPE_MD5.equals(aliPayConfig.getSign_type())) {
			return DigestUtils.md5Hex(data + aliPayConfig.getKey());
		}
		return "";
	}
	
	private boolean verify(Map<String, String> params) {
		if (params == null || params.size() == 0) {
			return false;
		}
		// 判断responsetTxt是否为true，isSign是否为true
		// responsetTxt的结果不是true，与服务器设置问题、合作身份者ID、notify_id一分钟失效有关
		// isSign不是true，与安全校验码、请求时的参数格式（如：带自定义参数等）、编码格式有关
		String responseTxt = "false";
		if (params.get("notify_id") != null) {
			String notify_id = params.get("notify_id");
			responseTxt = verifyResponse(notify_id, aliPayConfig.getPartner(), aliPayConfig.getHttps_verify_url());
		}
		String sign = params.get("sign");
		this.filterParams(params);
		String sign2 = this.getSign(this.jointLinkString(params));
		String trade_status = params.get("trade_status");
		if ("true".equals(responseTxt) && !Strings.isNullOrEmpty(sign) && sign.equals(sign2) && ("TRADE_FINISHED".equals(trade_status) || "TRADE_SUCCESS".equals(trade_status))) {
			return true;
		}
		return false;
	}
	
	private String verifyResponse(String notify_id,String partner,String https_verify_url) {
        //获取远程服务器ATN结果，验证是否是支付宝服务器发来的请求
        String veryfy_url = https_verify_url + "partner=" + partner + "&notify_id=" + notify_id;
        return checkUrl(veryfy_url);
    }
	
	private String checkUrl(String urlvalue) {
		String inputLine = "";
		try {
			URL url = new URL(urlvalue);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			inputLine = in.readLine().toString();
		} catch (Exception e) {
			e.printStackTrace();
			inputLine = "";
		}
		return inputLine;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private Map<String, String> getNotifyParams(HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		Map map = request.getParameterMap();
		logger.info("【zhifubao】支付宝回传参数：" + JSON.toJSONString(map));
		for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) (map.get(name));
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			// 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			// valueStr = new String(valueStr.getBytes("ISO-8859-1"), "UTF-8");
			/*
			 * String str = EncodingUtils.getEncoding(valueStr);
			 * System.out.println(str);
			 */
			params.put(name, valueStr);
		}
		return params;
	}
	


	@Override
	public String closeThirdPayOrder(String forderId) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map<String, String> getParameters(HttpServletRequest request, HttpServletResponse response) {
		Map<String,String> params=this.getNotifyParams(request);
		return params;
	}

}
