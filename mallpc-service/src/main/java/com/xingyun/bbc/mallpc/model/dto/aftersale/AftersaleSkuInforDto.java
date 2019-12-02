package com.xingyun.bbc.mallpc.model.dto.aftersale;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class AftersaleSkuInforDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("商品Id")
    private Long fgoodsId;

    @ApiModelProperty("skuId")
    private Long fskuId;

    @ApiModelProperty("sku名称")
    private String fskuName;

    @ApiModelProperty("sku主图")
    private String fskuThumbImage;

    @ApiModelProperty("sku贸易类型")
    private String ftradeType;

}
