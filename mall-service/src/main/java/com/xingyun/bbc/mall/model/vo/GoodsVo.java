package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("商品基本信息")
public class GoodsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品id")
    private Long fgoodsId;

    @ApiModelProperty(value = "商品编码")
    private String fgoodsCode;

    @ApiModelProperty(value = "商品名称")
    private String fgoodsName;

    @ApiModelProperty(value = "商品品牌Id")
    private Long fbrandId;

    @ApiModelProperty(value = "商品品牌名称")
    private String fbrandName;

    @ApiModelProperty(value = "商品品牌国家名称")
    private String fbrandCountryName;

    @ApiModelProperty(value = "商品品牌LOGO")
    private String fbrandLogo;

    @ApiModelProperty(value = "商品贸易类型Id")
    private Long ftradeId;

    @ApiModelProperty(value = "国旗图标")
    private String fcountryIcon;

    @ApiModelProperty(value = "商品贸易类型名称")
    private String ftradeType;

    @ApiModelProperty(value = "商品列表缩略图URL")
    private String fgoodsImgUrl;

    @ApiModelProperty(value = "商品详情富文本")
    private String fgoodsDetail;

    @ApiModelProperty(value = "商品描述--选中sku才展示")
    private String fskuDesc;

    @ApiModelProperty(value = "商品原产地Id")
    private Long foriginId;

    @ApiModelProperty(value = "商品原产地")
    private String fgoodsOrigin;

    @ApiModelProperty(value = "商品规格信息")
    private List<GoodsSkuVo> fgoodsSkuVo;

    @ApiModelProperty(value = "key:skuid value:商品名称 商品描述 商品列表缩略图")
    private List<GoodsAlterVo> goodsSkuAlterVo;

}
