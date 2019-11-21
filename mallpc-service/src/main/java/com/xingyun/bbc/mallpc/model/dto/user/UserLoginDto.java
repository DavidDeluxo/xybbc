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
public class UserLoginDto {

    @ApiModelProperty("账号")
    @NotBlank(message = "账号不能为空")
    private String userAccount;

    @ApiModelProperty("密码")
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty("是否自动登录 0否 1是")
    @NotNull(message = "是否自动登录不能为空")
    @NumberRange(values = {0,1},message = "是否自动登录参数值非法")
    private Integer isAutoLogin;

}
