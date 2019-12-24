package com.xingyun.bbc.mallpc.model.vo.article;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ArticleContentVo {
    @ApiModelProperty("文章id")
    private Integer farticleId;

    @ApiModelProperty("文章标题")
    private String farticleSubjectId;

    @ApiModelProperty("文章内容")
    private String farticleContent;
}
