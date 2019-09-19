package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class CityRegionVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 城市区域ID
     */
    @ApiModelProperty(value = "城市区域ID")
    private Integer fregionId;

    /**
     * 父类目ID
     */
    @ApiModelProperty(value = "父类目ID")
    private Integer fpRegionId;

    /**
     * 城市区域名称
     */
    @ApiModelProperty(value = "城市区域名称")
    private String fcrName;

}
