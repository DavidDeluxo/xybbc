package com.xingyun.bbc.mall.model.vo;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class OrderResultVo {
    @ApiModelProperty("支付操作状态码 200,处理成功 1029, 余额支付失败 1030,订单已支付 1031,订单已过期")
    private Integer code;

    @ApiModelProperty("余额支付状态：1还需要第三方支付，2支付成功")
    private Integer order_status;
    
    @ApiModelProperty("余额")
    private BigDecimal balance;

    @ApiModelProperty("消息")
    private String msg;

}
