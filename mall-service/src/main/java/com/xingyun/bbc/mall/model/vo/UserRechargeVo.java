package com.xingyun.bbc.mall.model.vo;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author:jianghui
 */
@Data
@ApiModel("用户充值")
public class UserRechargeVo{


    /** 充值单号 */
    @ApiModelProperty("充值单号")
    private String ftransId;

    /** 充值金额 */
    @ApiModelProperty("充值金额")
    private BigDecimal frecharger;

}