package com.xingyun.bbc.mall.infrastructure.message.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface SkuUpdateChannel {


    String SKU_PRICE_STOCK_INPUT = "sku_price_stock_input";

    @Input(SKU_PRICE_STOCK_INPUT)
    SubscribableChannel skuPriceStockInput();

    String SKU_BASE_INFO_INPUT = "sku_base_info_input";

    @Input(SKU_BASE_INFO_INPUT)
    SubscribableChannel skuBaseInfoInput();


}
