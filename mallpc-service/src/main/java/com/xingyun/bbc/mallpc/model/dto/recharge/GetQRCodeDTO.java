package com.xingyun.bbc.mallpc.model.dto.recharge;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author pengaoluo
 * @version 1.0.0
 */
@Data
public class GetQRCodeDTO {

    @ApiModelProperty("充值单号")
    @NotBlank(message = "充值单号不可为空")
    private String ftransId;
}
