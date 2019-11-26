package com.xingyun.bbc.mallpc.model.dto.detail;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("mall领取优惠券")
@Data
public class ReceiveCouponDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("优惠券id")
    private Long fcouponId;

    @ApiModelProperty("用户id")
    private Long fuid;

    @ApiModelProperty("券码")
    private String fcouponCode;
}
