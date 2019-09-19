package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("商品库存销量")
public class GoodStockSellVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "库存")
    private Long fstockRemianNum;

    @ApiModelProperty(value = "销量")
    private Long fsellNum;

}
