package com.xingyun.bbc.mall.infrastructure.message.listener;

import com.alibaba.fastjson.JSON;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.mall.infrastructure.message.channel.CouponChannel;
import com.xingyun.bbc.mall.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

@Slf4j
@EnableBinding(CouponChannel.class)
public class CouponReleaseListener {

    @Autowired
    private GoodsService goodsService;

    @StreamListener(CouponChannel.COUPON_RELEASE_INPUT)
    public void consumeCouponReleaseMessage(Coupon coupon){
        try {
            log.info("开始消费发布优惠券消息，message={}", JSON.toJSONString(coupon));
            goodsService.updateEsSkuWithCouponInfo(coupon);
            log.info("消费领取优惠券消息成功，message={}", JSON.toJSONString(coupon));
        } catch (Throwable e) {
            log.error("消费领取优惠券消息失败，message={}", JSON.toJSONString(coupon), e);
        }
    }




}
