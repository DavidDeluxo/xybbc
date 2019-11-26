package com.xingyun.bbc.mallpc.model.dto.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-20
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class ResetPasswordDto {

    @ApiModelProperty("手机号")
    @NotBlank(message = "手机号不能为空")
    private String fmobile;

    @ApiModelProperty("密码")
    @NotBlank(message = "密码不能为空")
    private String newPassword;

    @ApiModelProperty("验证码")
    private String verifyCode;

}
