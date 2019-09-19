package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author ZSY
 * @Description: 用户安全
 * @createTime: 2019-09-17 11:30
 */
@Data
public class UserSecurityDto {
    @ApiModelProperty("安全验证信息验证类型：1安全手机，2安全邮箱")
    private Integer requestType;

    @ApiModelProperty("用户ID")
    private Long fuid;

    @ApiModelProperty("客户端IP")
    private String ipAddress;

    @ApiModelProperty("设备识别码 IMEI")
    private String imei;

    @ApiModelProperty("手机号")
    private String fmobile;

    @ApiModelProperty("邮箱")
    private String fmail;

    @ApiModelProperty("验证码key")
    private String authNumKey;

    @ApiModelProperty("验证码")
    private String authNum;

    @ApiModelProperty("支付密码")
    private String fwithdrawPasswd;

    @ApiModelProperty("密码")
    private String fpasswd;

    @ApiModelProperty("是否触发滑块验证 0否 1是")
    private Integer isCheck;
}
