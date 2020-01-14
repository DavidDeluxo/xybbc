package com.xingyun.bbc.mall.common.enums;

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
public enum MessageTypeEnum {

    /**
     * 发货单发货
     */
    GOODS_SHIPPED(1),

    /**
     * 注册成功
     */
    REGISTER_SUCCESSED(2),

    /**
     * 修改手机号
     */
    MODIFY_NUMBER(3),

    /**
     * 优惠券到账
     */
    COUPON_RECEIVE(4),

    /**
     * 优惠券即将到期(24小时)
     */
    COUPON_ALMOST_OVERDUE(5),

    /**
     * 用户认证成功
     */
    USER_VERIFY(6),

    /**
     * 行云公告
     */
    XY_ANNOUNCEMENT(7),

    /**
     * 商品消息
     */
    GOODS_MESSAGE(8),

    /**
     * 其它
     */
    OTHER(9),

    /**
     * 专题活动
     */
    SUBJECT_ACTIVITY(10);

    private Integer code;

    MessageTypeEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static MessageTypeEnum getEnum(Integer code) {
        return Arrays.stream(values())
                .filter(value -> Objects.equals(code, value.getCode()))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultStatus.INTERNAL_SERVER_ERROR));
    }
}
