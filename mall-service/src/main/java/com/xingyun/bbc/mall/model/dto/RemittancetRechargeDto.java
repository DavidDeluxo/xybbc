package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RemittancetRechargeDto {

	@ApiModelProperty(value = "充值订单")
	private String forderId;
	
	@ApiModelProperty(value = "充值场景")
	private String payScene;
	
	@ApiModelProperty(value = "充值类型")
	private String payType;
	
	@ApiModelProperty(value = "凭证")
	private String payVoucher;
}
