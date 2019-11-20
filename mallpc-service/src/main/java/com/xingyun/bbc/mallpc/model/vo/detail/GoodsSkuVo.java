package com.xingyun.bbc.mallpc.model.vo.detail;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GoodsSkuVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品sku主键")
    private Long fskuId;

    /** sku编码 */
    @ApiModelProperty(value = "sku编码")
    private String fskuCode;

    /** 关联商品Id */
    @ApiModelProperty(value = "关联商品Id")
    private Long fgoodsId;

    /** sku名称 */
    @ApiModelProperty(value = "sku名称")
    private String fskuName;

    /** sku规格值 */
    @ApiModelProperty(value = "sku规格值")
    private String fskuSpecValue;

}
