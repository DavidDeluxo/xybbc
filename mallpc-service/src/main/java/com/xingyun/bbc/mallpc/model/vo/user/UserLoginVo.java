package com.xingyun.bbc.mallpc.model.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-18
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class UserLoginVo implements Serializable {

    private static final long serialVersionUID = -6249969860431090704L;

    @ApiModelProperty("用户ID")
    private Long fuid;

    @ApiModelProperty("用户姓名")
    private String funame;

    @ApiModelProperty("用户昵称")
    private String fnickname;

    @ApiModelProperty("用户是否可修改：0否，1是")
    private Integer funameIsModify;

    @ApiModelProperty("用户头像地址")
    private String fheadpic;

    @ApiModelProperty("用户等级")
    private Long fuserLevelId;

    @ApiModelProperty("用户免认证剩余天数")
    private String freeVerifyRemainDays;

    @ApiModelProperty("用户免认证到期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date freeVerifyEndTime;

    @ApiModelProperty("用户创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fcreateTime;

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
