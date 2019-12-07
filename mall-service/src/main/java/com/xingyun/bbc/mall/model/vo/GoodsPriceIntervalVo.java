package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("商品价格区间")
public class GoodsPriceIntervalVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "起始价格")
    private BigDecimal priceStart;

    @ApiModelProperty(value = "结尾价格")
    private BigDecimal priceEnd;

    @ApiModelProperty(value = "税费起始价格")
    private BigDecimal taxStart;

    @ApiModelProperty(value = "税费结尾价格")
    private BigDecimal taxEnd;

    @ApiModelProperty(value = "税费")
    private BigDecimal taxPrice;

//    @ApiModelProperty(value = "折合单价")
//    private BigDecimal dealUnitPrice;

//    @ApiModelProperty(value = "商品实际单价")
//    private BigDecimal realPrice;

}
