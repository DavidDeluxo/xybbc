package com.xingyun.bbc.mallpc.model.vo.detail;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("商品价格")
public class GoodsPriceVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "起始价格或费区间价格总价")
    private BigDecimal priceStart;

    @ApiModelProperty(value = "结尾价格")
    private BigDecimal priceEnd;

    @ApiModelProperty(value = "税费起始价格")
    private BigDecimal taxStart;

    @ApiModelProperty(value = "税费结尾价格")
    private BigDecimal taxEnd;

    @ApiModelProperty(value = "运费")
    private BigDecimal freightPrice;

    @ApiModelProperty(value = "税费")
    private BigDecimal taxPrice;

    @ApiModelProperty(value = "折合单价")
    private BigDecimal dealUnitPrice;

    @ApiModelProperty(value = "商品实际单价")
    private BigDecimal realPrice;

    @ApiModelProperty("省份名称")
    private String fdeliveryProvinceName;

    @ApiModelProperty("市名称")
    private String fdeliveryCityName;

    @ApiModelProperty("区/镇名称")
    private String fdeliveryAreaName;

    @ApiModelProperty("详细地址")
    private String fdeliveryAddr;

    @ApiModelProperty("商品类型  (1、新包装； 2、旧包装 ；3、新旧包装随机 ")
    private Integer fgoodsPackType;

    @ApiModelProperty("发货仓")
    private String fwarehouseName;

}
