package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author hekaijin
 * @date 2019/11/11 11:18
 * @Description
 */
@Data
@ApiModel("商品详情")
@Accessors(chain = true)
public class CouponGoodsDto extends SearchItemDto{

    @NotNull(message = "优惠券ID不能为空!")
    @ApiModelProperty("优惠券ID")
    private Long couponId;
}
