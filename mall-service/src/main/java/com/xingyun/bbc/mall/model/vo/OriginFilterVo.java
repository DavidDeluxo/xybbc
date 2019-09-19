package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("原产地")
public class OriginFilterVo {

    @ApiModelProperty("原产地id")
    private Integer foriginId;

    @ApiModelProperty("原产地名称")
    private String foriginName;


}
