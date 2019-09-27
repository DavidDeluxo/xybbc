package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ZSY
 * @Description: 登录返回
 * @createTime: 2019-09-03 11:30
 */
@Data
public class UserLoginVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    private Long fuid;

    @ApiModelProperty("用户姓名")
    private String funame;

    @ApiModelProperty("用户昵称")
    private String fnickname;

    @ApiModelProperty("用户头像地址")
    private String fheadpic;

    @ApiModelProperty("用户等级")
    private Long fuserLevelId;

    @ApiModelProperty("用户等级")
    private String flevelName;

    @ApiModelProperty("认证类型：1实体门店，2网络店铺，3网络平台，4批采企业，5微商代购")
    private Integer foperateType;

    @ApiModelProperty("冻结或禁用状态 ：1正常，2冻结，3禁用")
    private Integer ffreezeStatus;

    @ApiModelProperty("用户状态：1未认证，2 认证中，3 已认证，4未通过 ")
    private Integer fverifyStatus;

    @ApiModelProperty("支付密码状态 0未设置 1已设置 ")
    private Integer fwithdrawPasswdStatus;

    @ApiModelProperty("注册来源 android，ios，web")
    private String fregisterFrom;

    @ApiModelProperty("token 有效时长")
    private Long expire;

    @ApiModelProperty("token")
    private String token;

    @ApiModelProperty("手机号")
    private String fmobile;

    @ApiModelProperty("邮箱")
    private String fmail;

}
