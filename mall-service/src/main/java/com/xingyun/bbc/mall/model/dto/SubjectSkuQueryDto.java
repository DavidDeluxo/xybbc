package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SubjectSkuQueryDto {

    /**
     * 分类id集合
     */
    @ApiModelProperty(value = "分类id集合")
    private Map<String, List<Long>> categoryIds;

    /**
     * 品牌id集合
     */
    @ApiModelProperty(value = "品牌id集合")
    private List<Long> brandIds;

    /**
     * 标签id集合
     */
    @ApiModelProperty(value = "标签id集合")
    private List<Long> labelIds;

    /**
     * 贸易类型id
     */
    @ApiModelProperty(value = "贸易类型id")
    private List<Long> tradeIds;

    /**
     * sku 编码
     */
    @ApiModelProperty(value = "sku 编码")
    private String skuCode;
}
