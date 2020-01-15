package com.xingyun.bbc.mall.infrastructure.message.listener;

import com.alibaba.fastjson.JSON;
import com.xingyun.bbc.core.market.event.CouponEvent;
import com.xingyun.bbc.core.market.event.CouponInvalidateEvent;
import com.xingyun.bbc.core.market.event.CouponReleaseEvent;
import com.xingyun.bbc.core.market.event.channel.CouponEventInputChannel;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.operate.event.SubjectEvent;
import com.xingyun.bbc.core.operate.event.SubjectInvalidateEvent;
import com.xingyun.bbc.core.operate.event.SubjectReleaseEvent;
import com.xingyun.bbc.core.operate.po.Subject;
import com.xingyun.bbc.event.Event;
import com.xingyun.bbc.mall.infrastructure.message.event.SkuBaseInfoUpdateEvent;
import com.xingyun.bbc.mall.infrastructure.message.event.SkuStockPriceUpdateEvent;
import com.xingyun.bbc.mall.service.GoodsService;
import com.xingyun.bbc.mall.service.SubjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

@EnableBinding(CouponEventInputChannel.class)
@Slf4j
public class CouponReleaseListener {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private SubjectService subjectService;

    @StreamListener(CouponEventInputChannel.COUPON_EVENT_INPUT)
    public void consumeCouponReleaseMessage(Event event) {
        try {
            if (event instanceof CouponReleaseEvent) {

                CouponEvent couponEvent = (CouponEvent) event;
                log.info("开始消费发布优惠券信息message={}", JSON.toJSONString(couponEvent));
                Coupon coupon = couponEvent.getCoupon();
                goodsService.updateCouponInfoToEsByAlias(coupon, true);
                log.info("消费发布优惠券信息成功message={}", JSON.toJSONString(couponEvent));

            } else if (event instanceof CouponInvalidateEvent) {

                CouponEvent couponEvent = (CouponEvent) event;
                log.info("开始消费失效优惠券信息message={}", JSON.toJSONString(couponEvent));
                Coupon coupon = couponEvent.getCoupon();
                goodsService.deleteCouponInfoFromEsByAlias(coupon);
                log.info("消费发布失效优惠券信息成功message={}", JSON.toJSONString(couponEvent));

            } else if (event instanceof SubjectReleaseEvent) {

                SubjectEvent subjectEvent = (SubjectEvent) event;
                log.info("开始消费专题活动发布信息message={}", JSON.toJSONString(subjectEvent));
                Subject subject = subjectEvent.getSubject();
                subjectService.updateSubjectInfoToEsByAlias(subject);
                log.info("消费专题活动发布信息成功message={}", JSON.toJSONString(subjectEvent));

            } else if (event instanceof SubjectInvalidateEvent) {

                SubjectEvent subjectEvent = (SubjectEvent) event;
                log.info("开始消费专题活动发布信息message={}", JSON.toJSONString(subjectEvent));
                Subject subject = subjectEvent.getSubject();
                subjectService.deleteCouponInfoFromEsByAlias(subject);
                log.info("消费专题活动失效信息成功message={}", JSON.toJSONString(subjectEvent));

            } else if (event instanceof SkuStockPriceUpdateEvent) {

                SkuStockPriceUpdateEvent skuStockPriceUpdateEvent = (SkuStockPriceUpdateEvent) event;
                log.info("开始消费SKU价格库存更新信息message={}", JSON.toJSONString(skuStockPriceUpdateEvent));
                goodsService.updateEsSkuWithBaseInfo(skuStockPriceUpdateEvent.getSkuInfoMap(), false);
                log.info("消费发SKU价格库存更新信息成功message={}", JSON.toJSONString(skuStockPriceUpdateEvent));

            } else if (event instanceof SkuBaseInfoUpdateEvent) {

                SkuBaseInfoUpdateEvent baseInfoUpdateEvent = (SkuBaseInfoUpdateEvent) event;
                log.info("开始消费SKU基本信息更新信息message={}", JSON.toJSONString(baseInfoUpdateEvent));
                goodsService.updateEsSkuWithBaseInfo(baseInfoUpdateEvent.getSkuInfoMap(), true);
                log.info("开始消费SKU基本信息更新信息message={}", JSON.toJSONString(baseInfoUpdateEvent));

            } else {
                log.error("不支持该事件类型");
            }
        } catch (Throwable e) {
            log.error("消费事件信息失败", e);
        }
    }
}
