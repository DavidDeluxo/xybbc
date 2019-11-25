package com.xingyun.bbc.mallpc.model.vo.coupon;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("优惠券")
public class CouponVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 优惠券ID
     */
    @ApiModelProperty("优惠券ID")
    private Long fcouponId;

    /**
     * 优惠券名称
     */
    @ApiModelProperty("优惠券名称")
    private String fcouponName;

    /**
     * 优惠券类型
     */
    @ApiModelProperty("优惠券类型")
    private Integer fcouponType;

    /**
     * 使用门槛金额
     */
    @ApiModelProperty("使用门槛金额")
    private BigDecimal fthresholdAmount;

    /**
     * 指定金额
     */
    @ApiModelProperty("指定金额")
    private BigDecimal fdeductionValue;

    /**
     * 有效期开始时间
     */
    @ApiModelProperty("有效期开始时间")
    private Date fvalidityStart;

    /**
     * 有效期结束时间
     */
    @ApiModelProperty("有效期开始时间")
    private Date fvalidityEnd;

    /**
     * 有效期类型，1有效期区间、2有效期天数
     */
    @ApiModelProperty("有效期类型，1有效期区间、2有效期天数")
    private Integer fvalidityType;

    /**
     * 有效期天数
     */
    @ApiModelProperty("有效期天数")
    private Integer fvalidityDays;

    /**
     * 发放类型
     */
    @ApiModelProperty("发放类型--1系统赠送、2页面领取、3新人注册、4会员认证、5首单完成、6订单满赠、7好友邀请、8券码激活")
    private Integer freleaseType;

    /**
     * 适用商品范围，1全部商品、2指定商品可用、3指定商品不可用
     */
    @ApiModelProperty("适用商品范围，1全部商品、2指定商品可用、3指定商品不可用")
    private Integer fapplicableSku;

    /** 优惠券状态，1未使用、2已使用、3已失效 */
    @ApiModelProperty("优惠券状态，1未使用、2已使用、3已失效")
    private Integer fuserCouponStatus;


}
