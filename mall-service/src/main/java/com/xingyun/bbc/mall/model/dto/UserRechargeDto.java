package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author:jianghui
 */
@Data
@ApiModel(value = "用户余额充值")
public class UserRechargeDto{


    /** 用户ID */
    @ApiModelProperty(hidden = true)
    private Long fuid;

    /** 充值金额 */
    @ApiModelProperty(value="充值金额")
    private String frecharger;
    
    /** 支付类型：1 支付宝  2 微信支付 3 汇付天下 4 线下汇款 */
    @ApiModelProperty(value="支付类型：1 支付宝  2 微信支付 3 汇付天下 4 线下汇款")
    private Integer frechargeType;

}