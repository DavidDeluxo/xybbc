package com.xingyun.bbc.mall.model.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;

@Data
@ApiModel("商品品牌")
public class BrandListVo {

    @ApiModelProperty(value = "品牌id")
    private Long fbrandId;

    @ApiModelProperty(value = "品牌名称")
    private String fbrandName;

    @ApiModelProperty(value = "品牌logo图片地址")
    private String fbrandLogo;

}
