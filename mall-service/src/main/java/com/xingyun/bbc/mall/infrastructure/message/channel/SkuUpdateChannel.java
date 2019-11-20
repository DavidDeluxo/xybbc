package com.xingyun.bbc.mall.infrastructure.message.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface SkuUpdateChannel {


    /**
     * *********************消费**********************
     */
    String SKU_UPDATE_INPUT = "sku_update_input";
    @Input(SKU_UPDATE_INPUT)
    SubscribableChannel skuUpdateInput();


    /**
     * *********************生产**********************
     */
    String SKU_UPDATE_OUTPUT = "sku_update_output";
    @Output(SKU_UPDATE_OUTPUT)
    MessageChannel skuUpdateOutput();

}
