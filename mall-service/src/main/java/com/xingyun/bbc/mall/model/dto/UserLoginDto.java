package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author ZSY
 * @Description: 登录
 * @createTime: 2019-09-03 11:30
 */
@Data
public class UserLoginDto {

    @ApiModelProperty("账号")
    private String userAccount;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("手机号")
    private String fmobile;

    @ApiModelProperty("验证码key")
    private String authNumKey;

    @ApiModelProperty("验证码")
    private String authNum;

    @ApiModelProperty("客户端IP")
    private String ipAddress;

    @ApiModelProperty("设备型号")
    private String deviceName;

    @ApiModelProperty("操作系统")
    private String osVersion;

    @ApiModelProperty("设备识别码 IMEI")
    private String imei;

    @ApiModelProperty("是否触发滑块验证 0否 1是")
    private Integer isCheck;

    @ApiModelProperty("是否触发手机号注册验证 0否 1是")
    private Integer isMobileCheck;
}
