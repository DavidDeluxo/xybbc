package com.xingyun.bbc.mall.infrastructure.message.listener;

import com.alibaba.fastjson.JSON;
import com.xingyun.bbc.core.market.event.CouponEvent;
import com.xingyun.bbc.core.market.event.CouponInvalidateEvent;
import com.xingyun.bbc.core.market.event.CouponReleaseEvent;
import com.xingyun.bbc.core.market.event.channel.CouponEventInputChannel;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.mall.infrastructure.message.CouponUpdateBySkuEvent;
import com.xingyun.bbc.mall.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

@EnableBinding(CouponEventInputChannel.class)
@Slf4j
public class CouponReleaseListener {

    @Autowired
    private GoodsService goodsService;

    @StreamListener(CouponEventInputChannel.COUPON_EVENT_INPUT)
    public void consumeCouponReleaseMessage(CouponEvent couponEvent) {
        try {
            if (couponEvent instanceof CouponReleaseEvent) {

                log.info("开始消费发布优惠券信息message={}", JSON.toJSONString(couponEvent));
                Coupon coupon = couponEvent.getCoupon();
                goodsService.updateCouponInfoToEsByAlias(coupon);
                log.info("消费发布优惠券信息成功message={}", JSON.toJSONString(couponEvent));

            } else if (couponEvent instanceof CouponInvalidateEvent) {

                log.info("开始消费失效优惠券信息message={}", JSON.toJSONString(couponEvent));
                Coupon coupon = couponEvent.getCoupon();
                goodsService.deleteCouponInfoFromEsByAlias(coupon);
                log.info("消费发布失效优惠券信息成功message={}", JSON.toJSONString(couponEvent));

            }else if (couponEvent instanceof CouponUpdateBySkuEvent){

                log.info("开始消费SKU更新信息message={}", JSON.toJSONString(couponEvent));
                CouponUpdateBySkuEvent skuEvent = (CouponUpdateBySkuEvent) couponEvent;
                goodsService.updateEsSkuWithSkuUpdate(skuEvent.getSkuSourceMap());
                log.info("消费发SKU更新信息成功message={}", JSON.toJSONString(couponEvent));

            }else {
                log.error("不支持该事件类型");
            }
        } catch (Throwable e) {
            log.error("消费发布优惠券信息失败", e);
        }
    }
}
