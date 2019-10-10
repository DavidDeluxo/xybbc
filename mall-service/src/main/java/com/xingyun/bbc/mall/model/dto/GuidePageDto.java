package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
/**
 * @author:feixiaojie
 */
public class GuidePageDto implements Serializable{

    private static final long serialVersionUID = 1L;
	
    /**类型:0引导页配置1启动页配置**/
    @ApiModelProperty("类型:0引导页配置1启动页配置")
    private Integer ftype;

    public Integer getFtype() {
      return ftype;
    }

    public void setFtype(Integer ftype) {
      this.ftype = ftype;
    }
}