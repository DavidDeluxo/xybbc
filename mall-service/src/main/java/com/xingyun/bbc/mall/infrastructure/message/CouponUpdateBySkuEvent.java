package com.xingyun.bbc.mall.infrastructure.message;

import com.xingyun.bbc.core.market.event.CouponEvent;

import java.util.Map;

public class CouponUpdateBySkuEvent extends CouponEvent {

    Map<String, Object> skuSourceMap;

    public Map<String, Object> getSkuSourceMap() {
        return skuSourceMap;
    }

    public void setSkuSourceMap(Map<String, Object> skuSourceMap) {
        this.skuSourceMap = skuSourceMap;
    }
}
