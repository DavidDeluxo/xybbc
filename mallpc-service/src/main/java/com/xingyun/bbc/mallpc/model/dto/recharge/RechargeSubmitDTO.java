package com.xingyun.bbc.mallpc.model.dto.recharge;

import com.xingyun.bbc.mallpc.common.utils.AccountUtil;
import com.xingyun.bbc.mallpc.model.validation.extensions.annotations.NumberRange;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * @author pengaoluo
 * @version 1.0.0
 */
@Data
public class RechargeSubmitDTO {

    @ApiModelProperty("充值方式: 1 支付宝  2 微信支付  4 线下汇款")
    @NumberRange(values = {1, 2, 4}, message = "充值方式数值只能为1,2,4")
    private Integer frechargeType;

    @ApiModelProperty(value = "金额 单位:分", hidden = true)
    private Long ftransAmount;

    @ApiModelProperty("金额")
    @NotNull(message = "金额不可为空")
    @Positive(message = "金额必须为正数")
    private BigDecimal ftransAmountShow;

    public void setFtransAmountShow(BigDecimal ftransAmountShow) {
        this.ftransAmountShow = ftransAmountShow;
        this.ftransAmount = AccountUtil.multiplyOneHundred(ftransAmountShow);
    }

    /**
     * 外部不能设置ftransAmount
     * @param ftransAmount
     */
    public void setFtransAmount(Long ftransAmount) {
    }
}
