package com.xingyun.bbc.mall.base.enums;

import lombok.Getter;

@Getter
public enum VerifyCategory {
    ONE(1,"母婴用品"),
    TWO(2,"美妆个护"),
    THREE(3,"食品保健"),
    FOUR(4,"生活用品"),
    ;

    private Integer code;
    private String msg;

    VerifyCategory(Integer code, String msg) {
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
        VerifyCategory[] specialStatusEnums = VerifyCategory.values();
        for (VerifyCategory specialStatusEnum : specialStatusEnums) {
            if (specialStatusEnum.getCode().equals(code)) {
                return specialStatusEnum.getMsg();
            }
        }
        return null;
    }
}
