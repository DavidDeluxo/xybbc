package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("贸易类型")
public class TradeFilterVo {

    @ApiModelProperty("贸易类型id")
    private Integer ftradeId;

    @ApiModelProperty("贸易类型名称")
    private String ftradeName;

}
