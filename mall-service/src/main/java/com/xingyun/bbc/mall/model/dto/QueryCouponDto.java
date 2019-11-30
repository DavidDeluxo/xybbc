package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("mall查询优惠券")
@Data
public class QueryCouponDto implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty("用户id")
    private Long userId;



}
