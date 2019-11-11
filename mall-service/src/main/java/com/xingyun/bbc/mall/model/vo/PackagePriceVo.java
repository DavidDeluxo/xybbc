package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("商品")
public class PackagePriceVo {


    @ApiModelProperty("价格")
    private BigDecimal fbatchSellPrice;


}
