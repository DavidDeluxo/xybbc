package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel("商品类目")
public class IndexGoodsCategoryVo {

    @ApiModelProperty("分类id")
    private Long fcategoryId;

    @ApiModelProperty("分类名称")
    private String fcategoryName;

    @ApiModelProperty("分类描述")
    private String fcategoryDesc;



}
