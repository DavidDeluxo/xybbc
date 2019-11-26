package com.xingyun.bbc.mallpc.model.vo.recharge;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author pengaoluo
 * @version 1.0.0
 */
@Data
public class RechargeSubmitVO {

    @ApiModelProperty("充值单号")
    private String ftransId;

    @ApiModelProperty("收款账户名")
    private String accountsName;

    @ApiModelProperty("收款账户号")
    private String accountsNum;

    @ApiModelProperty("开户银行")
    private String bank;

}
