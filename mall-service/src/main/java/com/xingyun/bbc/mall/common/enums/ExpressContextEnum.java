package com.xingyun.bbc.mall.common.enums;

import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;

import java.util.Arrays;
import java.util.Objects;

public enum ExpressContextEnum {

    ORDER_SUBMISSION_SUCCESSFUL(1, "订单提交成功"),

    ORDER_PAYMENT_SUCCESSFUL(2, "订单支付成功，正在处理你的订单"),

    ORDER_PAYMENT_SUCCESSFUL_WAITING_FOR_CUSTOMS_CLEARANCE(3, "订单支付成功，等待海关清关"),

    THE_WAREHOUSE_HAS_RECEICED_THE_ORDER(4, "仓库已接单"),

    WAREHOUSE_PROCESSING(5, "仓库处理中"),

    THE_PARCEL_HAS_BEEN_DELIVERED_FROM_THE_WAREHOUSE(6, "包裹已出库");

    private Integer code;

    private String desc;

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    ExpressContextEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ExpressContextEnum getEnum(Integer code) {
        return Arrays.stream(values())
                .filter(value -> Objects.equals(code, value.getCode()))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultStatus.INTERNAL_SERVER_ERROR));
    }
}
