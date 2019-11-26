package com.xingyun.bbc.mall.base.enums;

import lombok.Getter;

@Getter
public enum UserVerifyResultStatus {
    SHOP_NAME_NOT_EXIST(1,"请录入门店名称"),
    SHOP_ADDRESS_NOT_EXIST(2,"请录入门店地址"),
    COMPANY_ADDRESS_NOT_EXIST(3,"请录入企业地址"),
    DETAILED_ADDRESS_NOT_EXIST(4,"请录入详细地址"),
    SHOP_FRONT_PIC_NOT_EXIST(5,"请录入门店照片"),
    BUSINESS_LICENSE_PIC_NOT_EXIST(6,"请录入营业执照"),
    IDCARD_NOT_EXIST(7,"请录入法人身份证"),
    PALTFORM_NOT_EXIST(8,"请录入销售平台"),
    SHOP_WEB_NOT_EXIST(9,"请录入店铺网址"),
    USER_IDCARD_NOT_EXIST(10,"请录入个人身份证"),
    PALTFORM_NAME_NOT_EXIST(11,"请录入平台名称"),
    PALTFORM_WEB_NOT_EXIST(12,"请录入平台网址"),
    COMPANY_NAME_NOT_EXIST(13,"请录入企业名称"),
    FUNAME_NOT_EXIST(14,"微商名称"),
    OPERATE_TYPE_NOT_EXIST(15,"认证类型缺失"),
    BUSINESS_LICENSE_PIC_NO_NOT_EXIST(16,"请录入营业执照号码"),
    BUSINESS_LICENSE_PIC_NO_CERTIFICATION(17,"该营业执照号码已被认证"),
    ;

    private Integer code;
    private String msg;

    UserVerifyResultStatus(Integer code, String msg) {
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
        UserVerifyResultStatus[] specialStatusEnums = UserVerifyResultStatus.values();
        for (UserVerifyResultStatus specialStatusEnum : specialStatusEnums) {
            if (specialStatusEnum.getCode().equals(code)) {
                return specialStatusEnum.getMsg();
            }
        }
        return null;
    }

}
