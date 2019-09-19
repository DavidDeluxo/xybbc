package com.xingyun.bbc.mall.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("商品分类")
public class CategoryFilterVo implements Comparable<CategoryFilterVo>{

    @ApiModelProperty("分类id")
    private Integer fcategoryId;

    @ApiModelProperty("分类名称")
    private String fcategoryName;

    @ApiModelProperty("分类排序")
    private Integer fcategorySort;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date fcreateTime;

    @ApiModelProperty("子类目列表")
    private List<CategoryFilterVo> childrenList;

    @Override
    public int compareTo(CategoryFilterVo compare) {
        int i = this.getFcategorySort().compareTo(compare.getFcategorySort());
        if (i == 0) {
            i = this.getFcreateTime().compareTo(compare.fcreateTime);
        }
        return i;
    }
}
