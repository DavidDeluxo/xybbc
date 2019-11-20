package com.xingyun.bbc.mall.infrastructure.message.sender;

import com.alibaba.fastjson.JSON;
import com.xingyun.bbc.core.market.event.CouponEvent;
import com.xingyun.bbc.core.market.event.channel.CouponEventOutputChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;

@EnableBinding(CouponEventOutputChannel.class)
@Slf4j
public class CouponEventSender {

    @Autowired
    private CouponEventOutputChannel couponEventOutputChannel;

    @Async
    public void sendCouponEventMessage(CouponEvent couponEvent){
        boolean result = couponEventOutputChannel.couponEventOutput().send(MessageBuilder.withPayload(couponEvent).build(), 3000);
        if (result) {
            log.info("发送优惠券事件消息成功，message={}", JSON.toJSONString(couponEvent));
        } else {
            log.warn("发送发布优惠券事件消息失败，message={}", JSON.toJSONString(couponEvent));
        }
    }

}
