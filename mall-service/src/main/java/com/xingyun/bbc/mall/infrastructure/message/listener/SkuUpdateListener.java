package com.xingyun.bbc.mall.infrastructure.message.listener;


import com.xingyun.bbc.mall.infrastructure.message.CouponUpdateBySkuEvent;
import com.xingyun.bbc.mall.infrastructure.message.channel.SkuUpdateChannel;
import com.xingyun.bbc.mall.infrastructure.message.sender.CouponEventSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import java.util.Map;

@Slf4j
@EnableBinding(SkuUpdateChannel.class)
public class SkuUpdateListener {

    @Autowired
    CouponEventSender couponEventSender;

    @StreamListener(SkuUpdateChannel.SKU_UPDATE_INPUT)
    public void consumeCouponReleaseMessage(Map<String, Object> skuMap) {
        try {
            Map<String, Object> a = skuMap;
            CouponUpdateBySkuEvent couponUpdateBySkuEvent = new CouponUpdateBySkuEvent();
            couponUpdateBySkuEvent.setSkuSourceMap(skuMap);
            couponEventSender.sendCouponEventMessage(couponUpdateBySkuEvent);
        } catch (Throwable e) {
            log.error("消费发布优惠券信息失败", e);
        }
    }

}
