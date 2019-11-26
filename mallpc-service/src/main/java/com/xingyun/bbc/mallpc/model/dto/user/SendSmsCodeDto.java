package com.xingyun.bbc.mallpc.model.dto.user;

import com.xingyun.bbc.mallpc.model.validation.extensions.annotations.NumberRange;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-18
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class SendSmsCodeDto {

    @ApiModelProperty("手机号")
    @NotBlank(message = "手机号不能为空")
    private String fmobile;

    @ApiModelProperty("事件类型 0注册 1重置密码")
    @NumberRange(values = {0,1},message = "事件类型参数值非法")
    @NotNull(message = "事件类型参数值非法")
    private Integer sourceType;
}
