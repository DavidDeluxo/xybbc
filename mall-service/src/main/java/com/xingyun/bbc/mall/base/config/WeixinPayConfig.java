package com.xingyun.bbc.mall.base.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "weixin")
public class WeixinPayConfig{
	
	private String pay_url;
	private String query_url;
	private String close_url;
	private String app_id;
	private String mch_id;
	private String trade_type;
	private String api_key;
	private String sign_type;
	private String input_charset;
	
	public String getPay_url() {
		return pay_url;
	}
	public void setPay_url(String pay_url) {
		this.pay_url = pay_url;
	}
	public String getQuery_url() {
		return query_url;
	}
	public void setQuery_url(String query_url) {
		this.query_url = query_url;
	}
	public String getClose_url() {
		return close_url;
	}
	public void setClose_url(String close_url) {
		this.close_url = close_url;
	}
	public String getMch_id() {
		return mch_id;
	}
	public void setMch_id(String mch_id) {
		this.mch_id = mch_id;
	}
	public String getApp_id() {
		return app_id;
	}
	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}
	public String getTrade_type() {
		return trade_type;
	}
	public void setTrade_type(String trade_type) {
		this.trade_type = trade_type;
	}
	public String getApi_key() {
		return api_key;
	}
	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}
	public String getSign_type() {
		return sign_type;
	}
	public void setSign_type(String sign_type) {
		this.sign_type = sign_type;
	}
	public String getInput_charset() {
		return input_charset;
	}
	public void setInput_charset(String input_charset) {
		this.input_charset = input_charset;
	}
}
