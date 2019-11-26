package com.xingyun.bbc.mallpc.model.vo.pay;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class PayResultVo {

    @ApiModelProperty("支付状态：true已支付，false未支付")
    private boolean payStatus;

}
