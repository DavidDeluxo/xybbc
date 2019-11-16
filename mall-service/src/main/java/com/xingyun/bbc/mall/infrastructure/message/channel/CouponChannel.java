package com.xingyun.bbc.mall.infrastructure.message.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-11-09
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public interface CouponChannel {

    /**
     * *********************消费**********************
     */
    String COUPON_RELEASE_INPUT = "coupon_release_input";

    @Input(COUPON_RELEASE_INPUT)
    SubscribableChannel couponReleaseInput();


}
