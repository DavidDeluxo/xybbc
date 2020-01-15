package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
/**
 * @Description 消息信息
 * @ClassName MessageSelfInfoVo
 * @Author ming.yiFei
 * @Date 2019/12/23 10:23
 **/
@Data
public class MessageSelfInfoVo {

    @ApiModelProperty(value = "商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "sku ID")
    private Long skuId;

    @ApiModelProperty(value = "sku 数量")
    private Integer skuNum;

    @ApiModelProperty(value = "订单ID")
    private String orderId;

    @ApiModelProperty(value = "认证类型：1实体门店，2网络店铺，3网络平台，4批采企业，5微商代购")
    private Integer authenticationType;

    @ApiModelProperty(value = "收件人姓名")
    private String deliveryName;

    @ApiModelProperty(value = "物流单号")
    private String orderLogisticsNo;

    @ApiModelProperty(value = "快递类型")
    private Integer logisticsType;

    @ApiModelProperty(value = "优惠券ID")
    private Long couponId;

    @ApiModelProperty(value = "物流轨迹节点内容")
    private String trajectoryContext;

    @ApiModelProperty(value = "物流轨迹节点时间")
    private String trajectoryTime;

    @ApiModelProperty(value = "活动url")
    private String url;

    @ApiModelProperty(value = "过期 0否 1是")
    private Integer expire;

    @ApiModelProperty(value = "专题id")
    private Long fsubjectId;
}
