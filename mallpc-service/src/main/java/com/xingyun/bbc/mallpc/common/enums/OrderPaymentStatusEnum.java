package com.xingyun.bbc.mallpc.common.enums;

public enum OrderPaymentStatusEnum {

    WAIT_PAY(1, "待付款"),

    ALREADY_PAY(2, "已付款"),

    ALREADY_CANCEL(3, "已取消");


    private Integer forderStatus;

    private String statusDesc;

    OrderPaymentStatusEnum(Integer forderStatus, String statusDesc) {
        this.forderStatus = forderStatus;
        this.statusDesc = statusDesc;
    }

    public static OrderPaymentStatusEnum getByStatus(Integer forderStatus) {
        if (forderStatus == null) {
            return null;
        }
        for (OrderPaymentStatusEnum em : OrderPaymentStatusEnum.values()) {
            if (em.forderStatus.equals(forderStatus)) {
                return em;
            }
        }
        return null;
    }

    public Integer getForderStatus() {
        return forderStatus;
    }

    public String getStatusDesc() {
        return statusDesc;
    }
}
