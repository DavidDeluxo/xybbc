package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("商品详情")
public class GoodsDetailMallDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "fuid")
    private Long fuid;

    @ApiModelProperty(value = "关联商品Id")
    private Long fgoodsId;

    @ApiModelProperty(value = "商品skuId")
    private Long fskuId;

    @ApiModelProperty(value = "批次id")
    private String fsupplierSkuBatchId;

    @ApiModelProperty(value = "包装规格Id")
    private Long fbatchPackageId;

    @ApiModelProperty(value = "购买数量")
    private Long fnum;

    @ApiModelProperty(value = "规格数量")
    private Long fbatchPackageNum;

    @ApiModelProperty("市ID")
    private Long fdeliveryCityId;
}
