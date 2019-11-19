package com.xingyun.bbc.mall.base.enums;

import lombok.Getter;

@Getter
public enum VerifyPlatform {
    TAOBAO(1,"淘宝"),
    TMALL(2,"天猫"),
    JDCOM(3,"京东"),
    SUNING(4,"苏宁"),
    BEIBEI(5,"贝贝"),
    YHD(6,"1号店"),
    JUMEI(7,"聚美"),
    VIPSHOP(8,"唯品会"),
    MIA(9,"蜜芽"),
    HAIZIWANG(10,"孩子王"),
    KAOLA(11,"考拉"),
    PINDUODUO(12,"拼多多"),
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
