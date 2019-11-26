package com.xingyun.bbc.mallpc.model.vo.withdraw;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author hekaijin
 * @date 2019/9/16 16:43
 * @Description
 */
@Data
@Accessors(chain = true)
@ApiModel("钱包金额")
public class WalletAmountVo {

    @ApiModelProperty("可用余额")
    private BigDecimal balance = new BigDecimal("0.00");

    @ApiModelProperty("待收益金额")
    private BigDecimal waitIncome = new BigDecimal("0.00");

    @ApiModelProperty("提现中的金额")
    private BigDecimal withdrawalAmount = new BigDecimal("0.00");
}
