package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("商品属性")
public class GoodsAttributeVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品品类属性id")
    private String fclassAttributeId;

    @ApiModelProperty(value = "商品品类属性名")
    private String fclassAttributeName;

    @ApiModelProperty(value = "商品品类属性值")
    private String fclassAttributeItemVal;

}
