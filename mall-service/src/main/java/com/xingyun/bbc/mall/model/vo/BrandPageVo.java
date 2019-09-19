package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;

@Data
public class BrandPageVo {

    @ApiModelProperty("品牌id")
    private Long fbrandId;

    @ApiModelProperty("品牌名称")
    private String fbrandName;

    @ApiModelProperty("品牌logo")
    private String fbrandLogo;

    @ApiModelProperty("品牌描述")
    private String fbrandDesc;

    @ApiModelProperty("品牌海报")
    private String fbrandPoster;

    @ApiModelProperty("原产地id")
    private Integer foriginId;

    @ApiModelProperty("原产地名称")
    private String foriginName;

    @ApiModelProperty("品牌商品数量")
    private Integer fgoodsTotalCount;

}
