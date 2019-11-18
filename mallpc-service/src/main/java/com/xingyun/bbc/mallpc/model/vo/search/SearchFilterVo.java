package com.xingyun.bbc.mallpc.model.vo.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("搜索过滤信息")
public class SearchFilterVo {

    @ApiModelProperty("品牌列表")
    private List<BrandFilterVo> brandList;

    @ApiModelProperty("类目列表")
    private List<CategoryFilterVo> categoryList;

    @ApiModelProperty("原产地列表")
    private List<OriginFilterVo> originList;

    @ApiModelProperty("贸易类型列表")
    private List<TradeFilterVo> tradeList;

    @ApiModelProperty("商品总数")
    private Integer totalCount;

    @ApiModelProperty("商品属性值列表")
    private List<GoodsAttributeFilterVo> attributeFilterList;

}
