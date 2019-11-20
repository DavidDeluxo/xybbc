package com.xingyun.bbc.mallpc.model.vo.detail;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * SKU批次包装规格关联表
 * @author:admin
 */
@Data
@ApiModel("商品包装规格")
public class GoodsSkuBatchPackageVo implements Serializable {

    private static final long serialVersionUID = 1L;
	
    /** 自增主键 */
    @ApiModelProperty(value = "包装规格自增主键")
    private Long fbatchPackageId;

    /** 关联批次Id */
    @ApiModelProperty(value = "关联批次Id")
    private String fsupplierSkuBatchId;

    /** 包装规格值 */
    @ApiModelProperty(value = "包装规格值")
    private Long fbatchPackageNum;

    /** 起发数 */
    @ApiModelProperty(value = "起发数")
    private Long fbatchStartNum;

    /** 采购单价 */
    @ApiModelProperty(value = "采购单价")
    private BigDecimal fbatchPackagePrice;

}