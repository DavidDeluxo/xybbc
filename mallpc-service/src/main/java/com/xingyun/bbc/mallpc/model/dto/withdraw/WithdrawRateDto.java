package com.xingyun.bbc.mallpc.model.dto.withdraw;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author hekaijin
 * @date 2019/9/19 15:43
 * @Description
 */
@Data
@Accessors(chain = true)
@ApiModel("获取提现手续费请求")
public class WithdrawRateDto implements Serializable {

    private static final long serialVersionUID = -7734975977807598839L;

    @ApiModelProperty("提现方式：1支付宝，2银行卡, 3微信")
    private Integer fwithdrawType;

}
