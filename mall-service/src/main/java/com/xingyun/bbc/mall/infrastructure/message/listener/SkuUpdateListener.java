package com.xingyun.bbc.mall.infrastructure.message.listener;


import com.xingyun.bbc.mall.infrastructure.message.channel.SkuUpdateChannel;
import com.xingyun.bbc.mall.infrastructure.message.event.SkuBaseInfoUpdateEvent;
import com.xingyun.bbc.mall.infrastructure.message.event.SkuStockPriceUpdateEvent;
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


    @StreamListener(SkuUpdateChannel.SKU_PRICE_STOCK_INPUT)
    public void consumeSkuStockPriceMessage(Map<String, Object> inputMap){
        try {
            SkuStockPriceUpdateEvent skuStockPriceUpdateEvent = new SkuStockPriceUpdateEvent();
            skuStockPriceUpdateEvent.setSkuInfoMap(inputMap);
            couponEventSender.sendCouponEventMessage(skuStockPriceUpdateEvent);
        }catch (Exception e){
            log.error("消费sku库存价格信息变更失败", e);
        }
    }

    @StreamListener(SkuUpdateChannel.SKU_BASE_INFO_INPUT)
    public void consumeSkuBaseInfoMessage(Map<String, Object> inputMap){
        try {
            SkuBaseInfoUpdateEvent skuBaseInfoUpdateEvent = new SkuBaseInfoUpdateEvent();
            skuBaseInfoUpdateEvent.setSkuInfoMap(inputMap);
            couponEventSender.sendCouponEventMessage(skuBaseInfoUpdateEvent);
        }catch (Exception e){
            log.error("消费sku基本信息变更失败", e);
        }
    }

}
