package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/1/13 10:15
 * @package com.xingyun.bbc.mall.model.dto
 */
@Data
@ApiModel("专题查询")
public class SubjectQueryDto {

    /** 专题id */
    @ApiModelProperty("专题id")
    @NotNull(message = "专题id不能为空")
    private Long fsubjectId;

}