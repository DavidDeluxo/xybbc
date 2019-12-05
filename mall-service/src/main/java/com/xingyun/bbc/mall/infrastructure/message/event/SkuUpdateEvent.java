package com.xingyun.bbc.mall.infrastructure.message.event;

import com.xingyun.bbc.core.market.event.CouponEvent;
import com.xingyun.bbc.event.Event;

import java.util.Map;

public class SkuUpdateEvent extends CouponEvent {

    Map<String, Object> skuInfoMap;

    public Map<String, Object> getSkuInfoMap() {
        return skuInfoMap;
    }

    public void setSkuInfoMap(Map<String, Object> skuInfoMap) {
        this.skuInfoMap = skuInfoMap;
    }
}
