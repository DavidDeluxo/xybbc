package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ArticleContentDto {
    @ApiModelProperty("文章id")
    private Integer farticleId;

    @ApiModelProperty("文章标题")
    private String farticleSubjectId;
}
