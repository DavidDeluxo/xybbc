package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author ZSY
 * @Description: 注册
 * @createTime: 2019-09-03 11:30
 */
@Data
public class UserRegisterDto {
    @ApiModelProperty("手机号")
    private String fmobile;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("确认密码")
    private String passwordcheck;

    @ApiModelProperty("验证码key")
    private String authNumKey;

    @ApiModelProperty("验证码")
    private String authNum;

    @ApiModelProperty("邀请码")
    private String finviter;

    @ApiModelProperty("友盟设备id")
    private String deviceToken;

    @ApiModelProperty("注册来源：android，ios，web")
    private String fregisterFrom;

}
