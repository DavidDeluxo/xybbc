package com.xingyun.bbc.mall.model.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class CityRegionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 城市区域ID
     */
  /*  @ApiModelProperty("城市区域ID")
    private Integer fregionId;*/

    /**
     * 父类目ID
     */
    @ApiModelProperty("父类目ID")
    private Integer fpRegionId;

    /**
     * 城市区域名称
     */
    /*@ApiModelProperty("城市区域名称")
    private String fcrName;*/

    /**
     * 区域类型，1为国家；2为省/直辖市；3为地级市；4为区/县
     */
    @ApiModelProperty("区域类型，1为国家；2为省/直辖市；3为地级市；4为区/县")
    private Integer fRegionType;
}
