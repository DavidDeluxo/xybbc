package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("商品")
public class SearchItemVo {

    @ApiModelProperty("sku_id")
    private Integer fskuId;

    @ApiModelProperty("sku名称")
    private String fskuName;

    @ApiModelProperty("贸易类型id")
    private Integer ftradedId;

    @ApiModelProperty("贸易类型名称")
    private String ftradeName;

    @ApiModelProperty("销量")
    private Long fsellNum;

    @ApiModelProperty("价格")
    private BigDecimal fbatchSellPrice;

    @ApiModelProperty("商品图片")
    private String fimgUrl;

    @ApiModelProperty("商品(spu)id")
    private Integer fgoodsId;

    @ApiModelProperty("商品库存剩余数量")
    private Integer fremainTotal;

    @ApiModelProperty(value = "sku状态(1.已上架 2.已下架 3.待上架 4.新增)", hidden = true)
    private Integer fskuStatus;

    @ApiModelProperty("关联商品标签Id")
    private Integer flabelId;

    @ApiModelProperty(value = "用户认证类型",hidden = true)
    private Integer fuserTypeId;

}
