package com.xingyun.bbc.mallpc.model.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-20
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class UserRegisterCouponVo implements Serializable {

    private static final long serialVersionUID = 4137336700662313501L;

    @ApiModelProperty("优惠券ID")
    private Long fcouponId;

    @ApiModelProperty("优惠券名称")
    private String fcouponName;

    @ApiModelProperty("使用门槛金额")
    private BigDecimal thresholdAmount;

    @ApiModelProperty("优惠券类型，1满减券、2折扣券 ")
    private Integer fcouponType;

    @ApiModelProperty("优惠券抵扣值 满减前面加￥,折扣后面加'折'")
    private BigDecimal deductionValue;

    @ApiModelProperty("有效期开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date fvalidityStart;

    @ApiModelProperty("有效期结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date fvalidityEnd;

}
