package com.xingyun.bbc.mall.model.vo;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class GoodsAttributeFilterVo {

    @ApiModelProperty("商品属性id")
    private Integer fattributeId;

    @ApiModelProperty("商品属性名称")
    private String fattributeName;

    @ApiModelProperty("商品属性值列表")
    List<GoodsAttributeItemFilterVo> itemSubPairList;

}
