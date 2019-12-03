package com.xingyun.bbc.mallpc.model.dto.coupon;

import com.xingyun.bbc.mallpc.model.dto.PageDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("我的优惠券查询条件")
@Data
public class MyCouponDto extends PageDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户id")
    private Long fuid;

    @ApiModelProperty(value = "优惠券状态，1未使用、2已使用、3已作废")
    private Integer fuserCouponStatus;

}
