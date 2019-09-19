package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * @author:lll
 */
@Data
public class PageConfigDto{

    /** 导航栏位置(0Banner配置 1ICON配置 2专题位配置*/
    @ApiModelProperty(value = "导航栏位置(0Banner配置 1ICON配置 2专题位配置")
    private Integer fposition;
	



}