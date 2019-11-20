package com.xingyun.bbc.mall.infrastructure.message.listener;


import com.alibaba.fastjson.JSON;
import com.xingyun.bbc.mall.infrastructure.message.channel.SkuUpdateChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import java.util.Map;

@Slf4j
@EnableBinding(SkuUpdateChannel.class)
public class SkuUpdateListener {

    @StreamListener(SkuUpdateChannel.SKU_UPDATE_INPUT)
    public void consumeCouponReleaseMessage(Map<String, Object> skuMap) {
        try {
            Map<String, Object> a = skuMap;
            log.info(JSON.toJSONString(skuMap));
        } catch (Throwable e) {
            log.error("消费发布优惠券信息失败", e);
        }
    }

}
