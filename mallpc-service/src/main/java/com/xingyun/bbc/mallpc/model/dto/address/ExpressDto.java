package com.xingyun.bbc.mallpc.model.dto.address;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author ZSY
 * @version V1.0
 * @Title:
 * @Package
 * @Description:
 * @date
 * @company 版权所有 深圳市天行云供应链有限公司
 */
@Data
public class ExpressDto {
    @ApiModelProperty(value = "订单号",dataType ="string")
    @NotBlank(message = "订单号不能为空")
    private String forderId;

    @ApiModelProperty(value = "发货单号",dataType ="string")
    @NotBlank(message = "发货单号不能为空")
    private String ftransportOrderId;
}
