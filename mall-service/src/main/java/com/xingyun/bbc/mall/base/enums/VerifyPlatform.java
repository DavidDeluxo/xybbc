package com.xingyun.bbc.mall.base.enums;

import lombok.Getter;

@Getter
public enum VerifyPlatform {
    TAOBAO(1,"淘宝"),
    TMALL(2,"天猫"),
    JDCOM(3,"京东"),
    SUNING(4,"苏宁"),
    BEIBEI(5,"贝贝"),
    ;

    private Integer code;
    private String msg;

    VerifyPlatform(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    public static String getMessageByCode(int code) {
        VerifyPlatform[] specialStatusEnums = VerifyPlatform.values();
        for (VerifyPlatform specialStatusEnum : specialStatusEnums) {
            if (specialStatusEnum.getCode().equals(code)) {
                return specialStatusEnum.getMsg();
            }
        }
        return null;
    }
}
