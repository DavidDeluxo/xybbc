package com.xingyun.bbc.mallpc.common.enums;

import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Description
 * @ClassName MessagePushTypeEnum
 * @Author ming.yiFei
 * @Date 2019/12/23 19:58
 **/
public enum MessagePushTypeEnum {

    /**
     * 手动
     */
    MANUAL(0, "手动"),

    /**
     * 自动
     */
    AUTO(1, "自动");

    private Integer code;

    private String desc;

    MessagePushTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static MessagePushTypeEnum getEnum(Integer code) {
        return Arrays.stream(values())
                .filter(value -> Objects.equals(code, value.getCode()))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultStatus.INTERNAL_SERVER_ERROR));
    }
}
