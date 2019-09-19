package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户交易明细表
 * @author:lll
 */
@Data
public class UserWalletDetailTotalVo {



    /** 收入合计 */
    @ApiModelProperty("收入合计")
    private BigDecimal fincomeAmountTotal;

    /** 支出合计 */
    @ApiModelProperty("支出合计")
    private BigDecimal fexpenseAmountTotal;




	
}