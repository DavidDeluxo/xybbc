package com.xingyun.bbc.mallpc.model.dto.pay;

import com.xingyun.bbc.pay.model.dto.ThirdPayDto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BalancePayDto extends ThirdPayDto {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "支付密码")
	private String payPwd;
	
	@ApiModelProperty(value = "余额类型")
	private String balanceType;
}
