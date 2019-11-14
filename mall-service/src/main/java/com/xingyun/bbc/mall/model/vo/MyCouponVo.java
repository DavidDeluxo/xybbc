package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@ApiModel("我的优惠券")
@Data
public class MyCouponVo implements Serializable {

    private static final long serialVersionUID = -5136295543133970188L;

    @ApiModelProperty("我的优惠券")
    private PageVo<CouponVo> couponVo;

    @ApiModelProperty("未使用")
    private Integer unUsedNum;

    @ApiModelProperty("已使用")
    private Integer usedNum;

    @ApiModelProperty("已过期")
    private Integer expiredNum;

    @ApiModelProperty("当前时间")
    private Date nowDate;

}
