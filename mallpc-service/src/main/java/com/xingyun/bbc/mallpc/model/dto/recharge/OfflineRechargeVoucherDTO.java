package com.xingyun.bbc.mallpc.model.dto.recharge;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 线下汇款提交凭证
 * @author pengaoluo
 * @version 1.0.0
 */
@Data
public class OfflineRechargeVoucherDTO {

    @ApiModelProperty("充值单号")
    @NotBlank(message = "充值单号不能为空")
    private String ftransId;

    @ApiModelProperty("汇款凭证,不含ip地址")
    @NotBlank(message = "汇款凭证不能为空")
    private String fpayVoucher;

}
