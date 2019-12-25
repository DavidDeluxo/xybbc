package com.xingyun.bbc.mallpc.common.enums;

import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Description
 * @ClassName MessageManualTypeEnum
 * @Author ming.yiFei
 * @Date 2019/12/23 19:58
 **/
public enum MessageGroupTypeEnum {

    /**
     * 物流助手
     */
    LOGISTICS_ASSISTANT(1, "物流助手"),

    /**
     * 账户通知
     */
    ACCOUNT_NOTIFICATION(2, "账户通知"),

    /**
     * 优惠促销
     */
    PREFERENTIAL_PROMOTION(3, "优惠促销"),

    /**
     * 通知消息
     */
    NOTIFICATION_MESSAGE(4, "通知消息");

    private Integer code;

    private String desc;

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    MessageGroupTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MessageGroupTypeEnum getEnum(Integer code) {
        return Arrays.stream(values())
                .filter(value -> Objects.equals(code, value.getCode()))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultStatus.INTERNAL_SERVER_ERROR));
    }
}
