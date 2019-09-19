package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("商品各种规格")
public class GoodspecificationVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "包装规格扩展")
    private List<GoodspecificationExVo> items;

    @ApiModelProperty(value = "规格汇总")
    private List<GoodspecificationDetailVo> detailLis;

}
