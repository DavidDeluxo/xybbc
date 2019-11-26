package com.xingyun.bbc.mallpc.model.dto.address;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-18
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class UserAddressDetailsDto {

    @ApiModelProperty("用户收货地址ID")
    @NotNull(message = "用户收货地址ID不能为空")
    private Long fdeliveryUserId;
}
