package com.xingyun.bbc.mall.model.vo;

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

    @ApiModelProperty(value = "运费")
    private BigDecimal freightPrice;

    @ApiModelProperty(value = "税费")
    private BigDecimal taxPrice;

    @ApiModelProperty(value = "折合单价")
    private BigDecimal dealUnitPrice;

    @ApiModelProperty("省份名称")
    private String fdeliveryProvinceName;

    @ApiModelProperty("市名称")
    private String fdeliveryCityName;

    @ApiModelProperty("区/镇名称")
    private String fdeliveryAreaName;

    @ApiModelProperty("详细地址")
    private String fdeliveryAddr;


}
