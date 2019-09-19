package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper=false)
public class CategoryDto extends PageDto{

    @ApiModelProperty("一级类目Id")
    private Integer fcategoryId1;

    @ApiModelProperty("用户ID")
    private Long fuid;

}
