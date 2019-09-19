package com.xingyun.bbc.mall.base.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



/** 
* @ClassName: ThirdPayUtilFactory 
* @Description: 生成第三方支付接口实例
* @author Yangli
* @date 2018年12月11日 上午10:27:08
*  
*/
@Component
public class ThirdPayUtilFactory {

	// 支付类型
	public static final String PAY_TYPE_ALI = "1";// 支付宝原生
	public static final String PAY_TYPE_WEIXIN = "2"; // 微信
	public static final String PAY_TYPE_ALI_OPEN = "7";// 支付宝二维码

	@Autowired
	private AliPayUtil aliPayUtil;
	@Autowired
	private AliPayOpenUtil aliPayOpenUtil;
	@Autowired
	private WeixinPayUtil weixinPayUtil;


	/**
	* @Title: createThirdPayUtil 
	* @Description: 创建第三方支付工具类 
	* @param payType 支付类型。和PAY_TYPE_*对应
	* @return
	* @author Tito
	 */
	public ThirdPayUtil createThirdPayUtil(String payType) {
		return this.createThirdPayUtil(payType, true);
	}
	
	/**
	* @Title: createThirdPayUtil 
	* @Description: 生成第三方支付类型 
	* @param payType 支付类型
	* @param isRecursion 是否递归
	* @return
	* @author Tito
	 */
	private ThirdPayUtil createThirdPayUtil(String payType, boolean isRecursion) {
		switch (payType) {
		case PAY_TYPE_ALI:
			return aliPayUtil;
		case PAY_TYPE_ALI_OPEN:
			return aliPayOpenUtil;
		case PAY_TYPE_WEIXIN:
			return weixinPayUtil;
		default:
			return null;
		}
	}
	
	// APP不支持汇聚等
	public ThirdPayUtil createTerminalPayUtil(String payType){
		switch (payType) {
		case PAY_TYPE_ALI:
			return aliPayUtil;
		case PAY_TYPE_WEIXIN:
			return weixinPayUtil;
		default:
			return null;
		}
	}
}
