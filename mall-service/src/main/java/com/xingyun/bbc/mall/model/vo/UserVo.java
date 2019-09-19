package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class UserVo {
    @ApiModelProperty("用户ID")
    private Long fuid;

    @ApiModelProperty("用户姓名")
    private String funame;

    @ApiModelProperty("用户昵称")
    private String fnickname;

    @ApiModelProperty("用户头像地址")
    private String fheadpic;

    @ApiModelProperty("用户等级")
    private Long fuserLevel;

    @ApiModelProperty("认证类型：1实体门店，2网络店铺，3网络平台，4批采企业，5微商代购")
    private Integer foperateType;

    @ApiModelProperty("市场BD")
    private Long fmarketBdId;

    @ApiModelProperty("性别：1男，2女")
    private Integer fsex;

    @ApiModelProperty("生日 */")
    private Date fbirthday;

    @ApiModelProperty("固定号码")
    private String ftelephone;

    @ApiModelProperty("手机号码")
    private String fmobile;

    @ApiModelProperty("手机号是否验证：0否，1是")
    private Integer fmoblieIsValid;

    @ApiModelProperty("邮箱")
    private String fmail;

    @ApiModelProperty("邮箱是否验证：0否，1是 ")
    private Integer fmailIsValid;

    @ApiModelProperty("QQ号码")
    private String fqqNo;

    @ApiModelProperty("微信号")
    private String fwechatNo;

    @ApiModelProperty("旺旺号码")
    private String fwangwangNo;

    @ApiModelProperty("冻结或禁用状态 ：1正常，2冻结，3禁用")
    private Integer ffreezeStatus;

    @ApiModelProperty("用户状态：1未认证，2 认证中，3 已认证，4未通过 ")
    private Integer fverifyStatus;

    @ApiModelProperty("审核通过时间")
    private Date fuserValidTime;

    @ApiModelProperty("邀请码")
    private String finviter;

    @ApiModelProperty("注册来源 android，ios，web")
    private String fregisterFrom;

    @ApiModelProperty("是否删除：0否，1是")
    private Integer fisDelete;

    @ApiModelProperty("备注")
    private String fremark;

    @ApiModelProperty("身份证号")
    private String fidcardNo;

    @ApiModelProperty("身份证名")
    private String fidcardName;

    @ApiModelProperty("手机号码验证时间")
    private Date fmobileValidTime;

    @ApiModelProperty("邮箱验证通过时间")
    private Date femailValidTime;

    @ApiModelProperty("最后登录时间")
    private Date flastloginTime;

    @ApiModelProperty("支付密码状态 0未设置 1已设置")
    private Integer fwithdrawPasswdStatus;
}
