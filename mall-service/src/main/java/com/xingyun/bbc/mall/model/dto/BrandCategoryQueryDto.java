package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BrandCategoryQueryDto {

    @ApiModelProperty(value = "商品分类id")
    private Long fcategoryId;

}
