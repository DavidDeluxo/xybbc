package com.xingyun.bbc.mallpc.model.dto.article;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ArticleContentDto {
    @ApiModelProperty("文章id")
    private Integer farticleId;

    @ApiModelProperty("文章标题")
    private String farticleSubjectId;
}
