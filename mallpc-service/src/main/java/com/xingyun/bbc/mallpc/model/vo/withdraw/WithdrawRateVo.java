package com.xingyun.bbc.mallpc.model.vo.withdraw;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author hekaijin
 * @date 2019/9/19 15:27
 * @Description
 */
@Data
@Accessors(chain = true)
@ApiModel("会员提现费率")
public class WithdrawRateVo {

    @ApiModelProperty("提现方式：1支付宝，2银行卡, 3微信")
    private Integer fwithdrawType;

    @ApiModelProperty("提现费率")
    private BigDecimal frate;

    @ApiModelProperty("最低提现金额")
    private BigDecimal minimumAmount;

}
