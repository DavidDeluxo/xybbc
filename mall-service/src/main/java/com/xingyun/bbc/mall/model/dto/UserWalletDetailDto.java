package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 用户交易明细表
 *
 * @author:admin
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class UserWalletDetailDto extends PageDto {


    /**
     * 用户ID
     */
    @ApiModelProperty("用户ID")
    private Long fuid;


    /**
     * 0:收入,1:支出
     */
    @ApiModelProperty("0:收入,1:支出")
    private Integer queryType;


}