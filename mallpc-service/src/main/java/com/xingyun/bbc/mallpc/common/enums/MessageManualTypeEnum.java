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
public enum MessageManualTypeEnum {

    /**
     * 行云公告
     */
    XY_ANNOUNCEMENT(1, "行云公告"),

    /**
     * 商品消息
     */
    GOODS_MESSAGE(2, "商品消息"),

    /**
     * 其它
     */
    OTHER(3, "其它");

    private Integer code;

    private String desc;

    MessageManualTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static MessageManualTypeEnum getEnum(Integer code) {
        return Arrays.stream(values())
                .filter(value -> Objects.equals(code, value.getCode()))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultStatus.INTERNAL_SERVER_ERROR));
    }
}
