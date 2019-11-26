package com.xingyun.bbc.mallpc.common.enums;

/**
 * User: xuxianbei
 * Date: 2019/9/4
 * Time: 14:05
 * Version:V1.0
 */
public enum PermissionEnums {
    ACCESS_TOKEN("accessToken", "系统登录token"),
    ACCESS_TOKEN_XYID("xyId", "本次回话的用户id"),
    ACCESS_TOKEN_XYSUBJECT("xySubject", "本次回话的用户对象");
    private String code;
    private String desc;

    PermissionEnums(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
