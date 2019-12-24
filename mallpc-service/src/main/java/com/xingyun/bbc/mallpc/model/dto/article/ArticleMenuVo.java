package com.xingyun.bbc.mallpc.model.dto.article;

import com.xingyun.bbc.mallpc.model.vo.article.ArticleContentVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ArticleMenuVo {

    @ApiModelProperty("菜单id")
    private Long fmenuId;

    @ApiModelProperty("菜单名称")
    private String fmenuName;

    @ApiModelProperty("关联栏目id")
    private Integer fcolumnId;

    @ApiModelProperty("菜单排序")
    private Integer fmenuSort;

    @ApiModelProperty("文章顺序类型 0 使用栏目文章排序 1 自定义")
    private Integer farticleSortType;

    @ApiModelProperty("显示文章数量")
    private Integer farticleShowNumber;

    @ApiModelProperty("关联文章")
    private List<ArticleContentVo> ArticleContentList;

    @ApiModelProperty("创建时间")
    private Date fcreateTime;

    @ApiModelProperty("修改时间")
    private Date fmodifyTime;
}
