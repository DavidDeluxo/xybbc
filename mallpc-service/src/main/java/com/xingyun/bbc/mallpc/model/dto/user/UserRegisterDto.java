package com.xingyun.bbc.mallpc.model.dto.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-18
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class UserRegisterDto {

    @ApiModelProperty("手机号")
    private String fmobile;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("验证码")
    private String verifyCode;

    @ApiModelProperty("邀请码")
    private String finviter;

}
