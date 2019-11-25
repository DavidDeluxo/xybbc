package com.xingyun.bbc.mallpc.model.dto.pay;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CheckPayDto{

	@ApiModelProperty(value = "订单编号")
	private String forderId;
	
}
