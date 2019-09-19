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
    @ApiModelProperty(value="用户ID(登录用户不用传)")
    private Long fuid;

    /** 充值金额 */
    @ApiModelProperty(value="充值金额")
    private String frecharger;

}