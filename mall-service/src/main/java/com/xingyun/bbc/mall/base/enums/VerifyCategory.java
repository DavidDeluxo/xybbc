package com.xingyun.bbc.mall.base.enums;

import lombok.Getter;

@Getter
public enum VerifyCategory {
    SHAMPOO(1,"洗发露"),
    ENZYMES(2,"酵素"),
    LIVER_ROTECTING_TABLET(3,"护肝片"),
    FOOT_CARE(4,"足部护理"),
    MASSIVE_GAINER(5,"增肌粉"),
    MACA(6,"玛咖"),
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
