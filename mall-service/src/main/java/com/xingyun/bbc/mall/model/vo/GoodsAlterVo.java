package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GoodsAlterVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品描述--选中sku才展示")
    private String fskuDesc;

    @ApiModelProperty(value = "商品列表缩略图URL")
    private String fgoodsImgUrl;

    @ApiModelProperty(value = "商品名称")
    private String fgoodsName;

}
