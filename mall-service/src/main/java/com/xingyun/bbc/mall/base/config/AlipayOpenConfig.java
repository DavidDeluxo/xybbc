package com.xingyun.bbc.mall.base.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *  对接支付宝开放平台
 * 
 * 支付宝与商户交互时，需要用到两对RSA密钥，支付宝一对，商户一对。
 * 商户给支付宝发送信息时，使用商户自己的私钥对数据加密，支付宝获取到数据后使用商家上传的公钥进行解密。
 * 支付宝发送信息给商户时，使用支付宝私钥对数据进行加密，商户获取到支付宝加密的信息后使用支付宝公钥对数据进行解密，得到正确的数据。
 * 
 * @author Tito
 */
@Configuration
@ConfigurationProperties(prefix = "alipayopen")
public class AlipayOpenConfig {
	
	/**
	 * 调用的接口版本
	 */
	private String version;
	
	/**
	 * 支付宝分配给开发者的应用ID
	 */
	private String app_id;
	
	/**
	 * 商户的私钥
	 */
	private String private_key;
	
	/**
	 * 支付宝的公钥
	 */
	private String public_key;

	/**
	 * OpenAPI统一URL
	 */
	private String openapi_url;
	
	/**
	 * 仅支持JSON
	 */
	private String format;
	
	/**
	 * 请求使用的编码格式
	 */
	private String charset;
	
	/**
	 * 商户生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2
	 */
	private String sign_type;
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPrivate_key() {
		return private_key;
	}

	public void setPrivate_key(String private_key) {
		this.private_key = private_key;
	}

	public String getPublic_key() {
		return public_key;
	}

	public void setPublic_key(String public_key) {
		this.public_key = public_key;
	}

	public String getApp_id() {
		return app_id;
	}

	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}

	public String getOpenapi_url() {
		return openapi_url;
	}

	public void setOpenapi_url(String openapi_url) {
		this.openapi_url = openapi_url;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getSign_type() {
		return sign_type;
	}

	public void setSign_type(String sign_type) {
		this.sign_type = sign_type;
	}

}
