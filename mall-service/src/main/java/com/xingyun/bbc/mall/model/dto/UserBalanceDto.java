package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author:jianghui
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class UserBalanceDto{


    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long fuid;

    /** 充值金额 */
    @ApiModelProperty("充值金额")
    private Long frecharger;

}