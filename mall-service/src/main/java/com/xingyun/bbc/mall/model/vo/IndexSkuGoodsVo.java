package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


import java.math.BigDecimal;

/**
 * @author:lll
 */
@Data
public class IndexSkuGoodsVo {


    /**
     * sku名称
     */
    @ApiModelProperty("sku名称")
    private String fskuName;

    /**
     * sku主图
     */
    @ApiModelProperty("sku主图")
    private String fskuThumbImage;


    @ApiModelProperty("价格")
    private BigDecimal fbatchSellPrice;

    @ApiModelProperty("销量")
    private Long fsellNum;

    @ApiModelProperty("skuId")
    private Long fskuId;

    @ApiModelProperty("goodsId")
    private Long fgoodsId;

    /**
     * 关联批次号
     */
    @ApiModelProperty("关联批次号")
    private String fsupplierSkuBatchId;

    /**
     * 关联包装规格Id
     */
    @ApiModelProperty("关联包装规格Id")
    private Long fbatchPackageId;


}