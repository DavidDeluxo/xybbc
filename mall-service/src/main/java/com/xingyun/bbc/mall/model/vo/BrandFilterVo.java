package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("品牌")
public class BrandFilterVo {

    @ApiModelProperty("品牌id")
    private Integer fbrandId;

    @ApiModelProperty("品牌名称")
    private String fbrandName;

}
