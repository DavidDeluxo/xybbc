package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 用户交易明细表
 * @author:admin
 */

@Data
public class UserWalletQueryDto {


    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long fuid;

    /** 当月开始日期 */
    @ApiModelProperty("当月开始日期")
    private Date startTime;

    /** 当月结束日期 */
    @ApiModelProperty("当月结束日期")
    private Date endTime;




	
}