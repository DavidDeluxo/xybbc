package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CouponLinkDto {
    @ApiModelProperty("优惠券链接")
    private String couponLink;

    @ApiModelProperty("用户ID")
    private Long fuid;
}
