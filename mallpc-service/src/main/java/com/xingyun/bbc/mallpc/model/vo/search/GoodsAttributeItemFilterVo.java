package com.xingyun.bbc.mallpc.model.vo.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("商品属性值")
public class GoodsAttributeItemFilterVo {

    @ApiModelProperty("商品属性值id")
    private Integer fattributeItemId;

    @ApiModelProperty("商品属性值名称")
    private String fattributeItemValueName;

}
