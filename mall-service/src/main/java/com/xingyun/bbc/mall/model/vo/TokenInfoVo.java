package com.xingyun.bbc.mall.model.vo;

import lombok.Data;

@Data
public class TokenInfoVo {

    Boolean isLogin;

    Integer fuid;

    /**
     * 认证类型：1实体门店，2网络店铺，3网络平台，4批采企业，5微商代购
     */
    Integer foperateType;

    /**
     * 用户状态：1未认证，2 认证中，3 已认证，4未通过
     */
    Integer fverifyStatus;

}
