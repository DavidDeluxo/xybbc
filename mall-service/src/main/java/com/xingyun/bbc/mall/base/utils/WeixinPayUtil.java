package com.xingyun.bbc.mall.base.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.xingyun.bbc.mall.base.config.ThirdPayConfig;
import com.xingyun.bbc.mall.base.config.WeixinPayConfig;
import com.xingyun.bbc.mall.common.enums.OrderPayMent.OrderPayEnum;
import com.xingyun.bbc.pay.model.dto.ThirdPayDto;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/** 
* @ClassName: WeixinPayUtil 
* @Description: 微信支付
* @author Yangli
* @date 2018年12月11日 上午10:27:08 
*  
*/
@Component
public class WeixinPayUtil implements ThirdPayUtil{
	
	private static Logger logger = LoggerFactory.getLogger(WeixinPayUtil.class);
	
	private static final String SUCCESS_STATUS = "SUCCESS";
	
	@Autowired
	private WeixinPayConfig wxPayConfig;
	@Autowired
	private ThirdPayConfig thirdPayConfig;


	@Override
	public Map<String, String> createThirdPayUrl(ThirdPayDto thirdPayDto) {
		String appVersion = thirdPayDto.getAppVersion(); // APP的版本号,用于控制返回url和回调判断是否APP支付
		Date now = new Date();
		//String spbill_create_ip_lis = GetRemoteHost.getRemoteHost(request); // 获取发起者ip
		//String spbill_create_ip = spbill_create_ip_lis.split(",")[0];
		//logger.info("spbill_create_ip :" + spbill_create_ip);
		String order_price = thirdPayDto.getPayAmount(); // 价格
		SortedMap<String, String> packageParams = new TreeMap<>();
		packageParams.put("appid", wxPayConfig.getApp_id()); // appid
		packageParams.put("mch_id", wxPayConfig.getMch_id());// 商户号
		packageParams.put("nonce_str", this.createNonce_str()); // 随机数
		packageParams.put("body", thirdPayDto.getForderId());// 商品描述
		packageParams.put("out_trade_no", thirdPayDto.getForderId());// 我们的订单号
		packageParams.put("total_fee", order_price); // 支付价格
        /*
            调用微信支付服务器的ip.
            同一个订单,IP变了的话, 生成二维码会失败.所以写死127.0.0.1.
            如果用的是用户的ip,那么会出现换电脑不能支付的情况
            如果用服务器的ip,集群的话,负载均衡时ip不同,生成二维码失败.
         */
		packageParams.put("spbill_create_ip", "127.0.0.1");
		packageParams.put("notify_url", thirdPayConfig.getPay_notify_request());// 回调接口
		packageParams.put("trade_type", wxPayConfig.getTrade_type());// 交易类型
		packageParams.put("attach", ThirdPayUtil.jointExtendParams(ThirdPayUtilFactory.PAY_TYPE_WEIXIN, thirdPayDto.getPayScene(), appVersion)); // 自定义扩展参数
		packageParams.put("time_start", DateUtils.formatDate(now)); // 交易起始时间
		if (ThirdPayUtil.PAY_SCENE_ORDER.equals(thirdPayDto.getPayScene())) {
			packageParams.put("time_expire", DateUtils.formatDate(thirdPayDto.getLockTime()));// 支付单超时时间
		}
		Map<String, String> wxResult = getWxPayResult(packageParams, wxPayConfig.getPay_url(),thirdPayDto.getPayScene());
		if (wxResult == null) {
			return ThirdPayUtil.fillThirdPayUrlInfo(null, null, "微信连接异常...");
		}
		boolean verify = checkIsSignValidFromResponseString(wxResult, wxPayConfig.getApi_key(), wxPayConfig.getInput_charset());
		String return_code = wxResult.get("return_code");
		String result_code = wxResult.get("result_code");
		String thirdPayUrl =  wxResult.get("code_url");


		if (!verify || !SUCCESS_STATUS.equals(return_code) || !SUCCESS_STATUS.equals(result_code)) {
			String returnMsg = wxResult.get("return_msg");
			String err_code = wxResult.get("err_code");
			String err_code_des = wxResult.get("err_code_des");
			logger.info("生成微信付款码失败, {}, {}, {}" , returnMsg, err_code, err_code_des);
			return ThirdPayUtil.fillThirdPayUrlInfo(null, null, "生成微信付款码失败! ");
		}
		if (!Strings.isNullOrEmpty(appVersion) && !"1".equals(thirdPayDto.getIsCreateAll())) {
			thirdPayUrl = this.concatAppUrl(wxResult, now);
		}
		return ThirdPayUtil.fillThirdPayUrlInfo(ThirdPayUtil.URL_TYPE_QR_CODE, thirdPayUrl, null);
	}

	
	@Override
	public Map<String, String> modifyParseInfoFromThirdPayResponse(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> params = this.checkNotify(request, response);
		if(params == null || params.size() == 0) {
			return null;
		}
		String forderId = params.get("out_trade_no"); // 外部订单号
		String payAmount = PriceUtil.toYuan((params.get("total_fee")).toString()).toString();
		String thirdTradeNo = params.get("transaction_id"); //微信支付订单号
		String payTime = params.get("time_end"); // 微信支付完成时间yyyyMMddHHmmss
		String bankCode = params.get("bank_type"); // 银行类型
		String payAccount = params.get("openid");
		String recieveAccount = params.get("mch_id"); // 商户号
		String thirdPayType = String.valueOf(OrderPayEnum.WECHAT_PAY.getValue()); 
		return ThirdPayUtil.fillThirdPayInfo(forderId, thirdPayType, payAmount, payTime, thirdTradeNo, bankCode, null, payAccount, null, recieveAccount, null);
	}
	
	@Override
	public String thirdPayNotifySuccess(HttpServletResponse response){
		responsePayNotify(response, true, "报文");
		return "";
	}
	
	// 生成随机数
	private String createNonce_str() {
		String currTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String strTime = currTime.substring(8, currTime.length());
		String strRandom = GenerateRandomNumUtil.generateAuthNum(4);
		return strTime + strRandom; 
	}
	
	// 拼接app url
	private String concatAppUrl(Map<String, String> wxResult, Date now) {
		Map<String, String> urlMap = new HashMap<>();
		urlMap.put("appid", wxResult.get("appid"));
		urlMap.put("partnerid", wxResult.get("mch_id"));
		urlMap.put("prepayid", wxResult.get("prepay_id"));
		urlMap.put("package", "Sign=WXPay");
		urlMap.put("noncestr", wxResult.get("nonce_str"));
		urlMap.put("timestamp", String.format("%010d", now.getTime() / 1000));
		urlMap.put("sign", this.getSign(urlMap, wxPayConfig.getApi_key(), wxPayConfig.getInput_charset()));
		urlMap.put("packageValue", urlMap.get("package"));
		urlMap.remove("package");
		return JSON.toJSONString(urlMap);
	}
	
	private Map<String, String> checkNotify(HttpServletRequest request, HttpServletResponse response) {
		String inputLine;
		StringBuffer notityXml = new StringBuffer();
		Map<String, String> params = new HashMap<>();
		try {
			while ((inputLine = request.getReader().readLine()) != null) {
				notityXml.append(inputLine);
			}
		} catch (Exception e) {
			logger.info("w_notityXml:"+notityXml.toString());
			logger.info("微信输入流为空或已经关闭。");
		}
		if (Strings.isNullOrEmpty(notityXml.toString())) {
			notityXml = notityXml.append(request.getAttribute("notityXml"));
			params = (Map<String, String>) request.getAttribute("notifyParams");
		} else {
			params = getMapFromXML(notityXml.toString());
		}
		
		try {
			request.getReader().close();
		} catch (IOException e) {
			logger.info("-----微信输入流为空或已经关闭。");
		}
		
		boolean flag = checkIsSignValidFromResponseString(params, wxPayConfig.getApi_key(), wxPayConfig.getInput_charset());

		String return_code = params.get("return_code");
		String result_code = params.get("result_code");
		if (!flag) {
			responsePayNotify(response, false, "验签失败");
			return null;
		}
		if (!SUCCESS_STATUS.equals(return_code)) {
			responsePayNotify(response, false, "通信标识不成功");
			return null;
		}
		if (!SUCCESS_STATUS.equals(result_code)) {
			responsePayNotify(response, false, "报文");
			return null;
		}
		return params;
	}
	
	public static Map<String, String> getMapFromXML(String xmlString) {
		// 这里用Dom的方式解析回包的最主要目的是防止API新增回包字段
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		InputStream is = getStringStream(xmlString);
		if (is == null) {
			return null;
		}
		Document document = null;
		try {
			document = builder.parse(is);
		} catch (SAXException | IOException e) {
			e.printStackTrace();
			return null;
		}
		// 获取到document里面的全部结点
		NodeList allNodes = document.getFirstChild().getChildNodes();
		Node node;
		Map<String, String> params = new HashMap<String, String>();
		int i = 0;
		while (i < allNodes.getLength()) {
			node = allNodes.item(i);
			if (node instanceof Element) {
				params.put(node.getNodeName(), node.getTextContent());
			}
			i++;
		}
		return params;
	}
	
	public static InputStream getStringStream(String sInputString) {
        ByteArrayInputStream tInputStringStream = null;
        if (sInputString != null && !sInputString.trim().equals("")) {
            tInputStringStream = new ByteArrayInputStream(sInputString.getBytes());
        }
        return tInputStringStream;
    }
	
	private Map<String, String> getWxPayResult(SortedMap<String, String> packageParams, String url,String payScene){
		String sign = createSign(packageParams, wxPayConfig.getApi_key());
		logger.info("【weixin】加密串：" + sign);
		packageParams.put("sign", sign);
		String requestXML = getRequestXml(packageParams);

        logger.info("【weixin】请求报文：" + requestXML);
		String resXml = postData(url, requestXML, null);


		Map<String, String> map = getMapFromXML(resXml);
		return map;
	}


	private String createSign(SortedMap<String, String> params, String API_KEY) {
		StringBuffer buf = new StringBuffer();
		for (Entry<String, String> entry : params.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (!Strings.isNullOrEmpty(value) && !"sign".equals(key) && !"key".equals(key)) {
				buf.append(key).append("=").append(value).append("&");
			}
		}
		buf.append("key=").append(API_KEY);
		return DigestUtils.md5Hex(buf.toString());
	}
	
	private String getRequestXml(SortedMap<String, String> params) {
		StringBuffer buf = new StringBuffer();
		buf.append("<xml>");
		for (Entry<String, String> entry : params.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if ("attach".equalsIgnoreCase(key) || "body".equalsIgnoreCase(key) || "sign".equalsIgnoreCase(key)) {
				buf.append("<").append(key).append(">").append("<![CDATA[").append(value).append("]]></").append(key).append(">");
			} else {
				buf.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
			}
		}
		buf.append("</xml>");
		return buf.toString();
	}
	
	private String postData(String urlStr, String data, String contentType) {
		BufferedReader reader = null;
		try {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			if (contentType != null){
				conn.setRequestProperty("content-type", contentType);
			}
			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
			if (data == null)
				data = "";
			writer.write(data);
			writer.flush();
			writer.close();

			reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			if (!StringUtils.isEmpty(sb)) {
				logger.info("服务端返回：" + sb.toString());
				return sb.toString();
			}
		} catch (IOException e) {
			logger.error("Error connecting to " + urlStr + ": " + e.getMessage());
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				logger.error("IOException:" + e.getMessage());
			}
		}
		return null;
	}
	
	private boolean checkIsSignValidFromResponseString(Map<String, String> map, String API_KEY, String characterEncoding) {
		logger.info("【weixin】签名 检验API返回数据" + map.toString());
		String signFromAPIResponse = map.get("sign").toString();
		if (signFromAPIResponse == "" || signFromAPIResponse == null) {
			logger.error("【weixin】API返回的数据签名数据不存在，有可能被第三方篡改!!!");
			return false;
		}
		logger.info("【weixin】服务器返回包里面的签名是:" + signFromAPIResponse);
		// 清掉返回数据对象里面的Sign数据（不能把这个数据也加进去进行签名），然后用签名算法进行签名
		map.put("sign", "");
		// 将API返回的数据根据用签名算法进行计算新的签名，用来跟API返回的签名进行比较
		String signForAPIResponse = getSign(map, API_KEY, characterEncoding);
		if (!signForAPIResponse.equalsIgnoreCase(signFromAPIResponse)) {
			// 签名验不过，表示这个API返回的数据有可能已经被篡改了
			logger.error("【weixin】API返回的数据签名验证不通过，有可能被第三方篡改!!!");
			return false;
		}
		logger.info("【weixin】恭喜，API返回的数据签名验证通过!!!");
		return true;
	}
	
	private String getSign(Map<String, String> map, String API_KEY, String characterEncoding) {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getValue() != "" && !"serialVersionUID".equals(entry.getKey())) {
				list.add(entry.getKey() + "=" + entry.getValue() + "&");
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		String result = sb.toString();
		result += "key=" + API_KEY;
		result = DigestUtils.md5Hex(result);
		return result;
	}
	
	private void responsePayNotify(HttpServletResponse response, boolean isSuccess, String remark) {
		String resXml = "<xml>" + "<return_code><![CDATA[" + (isSuccess ? SUCCESS_STATUS : "FAIL") + "]]></return_code>" + "<return_msg><![CDATA[" + remark + "]]></return_msg>" + "</xml> ";
		BufferedOutputStream out;
		try {
			out = new BufferedOutputStream(response.getOutputStream());
			out.write(resXml.getBytes());
			out.flush();
			out.close();
		} catch (IOException e) {
			logger.error("responsePayNotify");
		}
	}

	@Override
	public String closeThirdPayOrder(String forderId) {
		SortedMap<String, String> packageParams = new TreeMap<>();
		packageParams.put("appid", wxPayConfig.getApp_id()); // 商户appid
		packageParams.put("mch_id", wxPayConfig.getMch_id()); // 商户号
		packageParams.put("nonce_str", this.createNonce_str()); // 随机数
		packageParams.put("out_trade_no", forderId); // 订单号
		Map<String, String> wxResult = getWxPayResult(packageParams, wxPayConfig.getClose_url(),null);
		if (wxResult == null) {
			String errorMsg = "订单: " + forderId + "关闭微信二维码失败! 微信服务器异常...";
			logger.info(errorMsg);
			return errorMsg;
		}
		boolean virify = checkIsSignValidFromResponseString(wxResult, wxPayConfig.getApi_key(), wxPayConfig.getInput_charset());
		String return_code = wxResult.get("return_code");
		String result_code = wxResult.get("result_code");

		if (virify && SUCCESS_STATUS.equals(return_code) && SUCCESS_STATUS.equals(result_code)) {
			return null;
		}
		String returnMsg = wxResult.get("return_msg");
		String err_code = wxResult.get("err_code");
		String err_code_des = wxResult.get("err_code_des");
		logger.info("订单: {}关闭微信付款码失败, {}, {}, {}", forderId, returnMsg, err_code, err_code_des);
		return "关闭微信付款码失败: " + err_code_des;
	}


	@Override
	public Map<String, String> getParameters(HttpServletRequest request, HttpServletResponse response) {
		String inputLine;
		StringBuffer notityXml = new StringBuffer();
		Map<String, String> params = new HashMap<>();
		try {
			while ((inputLine = request.getReader().readLine()) != null) {
				notityXml.append(inputLine);
			}
		} catch (Exception e) {
			logger.info("w_notityXml:"+notityXml.toString());
			logger.info("微信输入流为空或已经关闭。");
		}
		if (Strings.isNullOrEmpty(notityXml.toString())) {
			notityXml = notityXml.append(request.getAttribute("notityXml"));
			params = (Map<String, String>) request.getAttribute("notifyParams");
		} else {
			params = getMapFromXML(notityXml.toString());
		}
		
		try {
			request.getReader().close();
		} catch (IOException e) {
			logger.info("-----微信输入流为空或已经关闭。");
		}
		return params;
	}
	
}
