package com.xingyun.bbc.mall.base.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 
* @ClassName: XyThirdPayConfig 
* @Description: 行云第三方支付配置
* @author Yangli
* @date 2018年12月11日 上午10:27:08 
*  
*/
@Component
@ConfigurationProperties(prefix = "thirdpay")
public class ThirdPayConfig {

	private String backend_domain;

	private String web_domain;

	private String pay_return_request;

	private String pay_notify_request;

	private String order_pay_success_request;
	
	private String recharge_success_request;

	private String micro_pay_success_request;
	
	private String customNotifyRequest;
	
	private String activePayType;
	
	/**
	 * @return the recharge_shuangqian_pay_success
	 */
	public String getRecharge_shuangqian_pay_success() {
		StringBuilder builder = new StringBuilder("http://");
		builder.append(web_domain).append(recharge_success_request);
		return builder.toString();
	}

	/**
	 * @return the order_shuangqian_pay_success
	 */
	public String getOrder_shuangqian_pay_success() {
		StringBuilder builder = new StringBuilder("http://");
		builder.append(web_domain).append(order_pay_success_request);
		return builder.toString();
	}

	public String getBackend_domain() {
		return backend_domain;
	}

	public void setBackend_domain(String backend_domain) {
		this.backend_domain = backend_domain;
	}

	public String getWeb_domain() {
		return web_domain;
	}

	public void setWeb_domain(String web_domain) {
		this.web_domain = web_domain;
	}

	public String getPay_return_request() {
		return this.getBackendUrl(pay_return_request);
	}

	public void setPay_return_request(String pay_return_request) {
		this.pay_return_request = pay_return_request;
	}

	public String getPay_notify_request() {
		return this.getBackendUrl(pay_notify_request);
	}

	public void setPay_notify_request(String pay_notify_request) {
		this.pay_notify_request = pay_notify_request;
	}

	public String getOrder_pay_success_request() {
		return this.getHtmlScript(order_pay_success_request);
	}

	public void setOrder_pay_success_request(String order_pay_success_request) {
		this.order_pay_success_request = order_pay_success_request;
	}

	public String getRecharge_success_request() {
		return this.getHtmlScript(recharge_success_request);
	}

	public void setRecharge_success_request(String recharge_success_request) {
		this.recharge_success_request = recharge_success_request;
	}

	public String getMicro_pay_success_request() {
		return this.getHtmlScript(micro_pay_success_request);
	}

	public void setMicro_pay_success_request(String micro_pay_success_request) {
		this.micro_pay_success_request = micro_pay_success_request;
	}

	private String getBackendUrl(String request) {
		StringBuilder builder = new StringBuilder("http://");
		builder.append(backend_domain).append(request);
		return builder.toString();
	}

	private String getHtmlScript(String request) {
		StringBuilder builder = new StringBuilder("<script>window.location.href='http://");
		builder.append(web_domain).append(request).append("'</script>");
		return builder.toString();
	}

	public String getCustomNotifyRequest() {
		return this.getBackendUrl(customNotifyRequest);
	}

	public void setCustomNotifyRequest(String customNotifyRequest) {
		this.customNotifyRequest = customNotifyRequest;
	}

	public String getActivePayType() {
		return activePayType;
	}

	public void setActivePayType(String activePayType) {
		this.activePayType = activePayType;
	}
	
}
