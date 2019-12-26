package com.xingyun.bbc.mall.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("商品类目")
public class GoodsCategoryVo implements Comparable<GoodsCategoryVo>{

    @ApiModelProperty("分类id")
    private Long fcategoryId;

    @ApiModelProperty("分类名称")
    private String fcategoryName;

    @ApiModelProperty("分类描述")
    private String fcategoryDesc;

    @ApiModelProperty("分类图片地址")
    private String fcategoryUrl;

    @ApiModelProperty("上级分类Id")
    private Long fparentCategoryId;

    @ApiModelProperty("是否推荐 0 否 1 是")
    private Integer fisRecommed;

    @ApiModelProperty("分类排序")
    private Integer fcategorySort;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date fcreateTime;

    @ApiModelProperty("修改时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date fmodifyTime;

    @ApiModelProperty("子类目列表")
    private List<GoodsCategoryVo> childrenList;

    @ApiModelProperty("热门品牌")
    private List<BrandListVo> hotBrandList;

    @Override
    public int compareTo(GoodsCategoryVo compare) {
        int i = 0;
        if(compare == null){
            throw new IllegalArgumentException();
        }

        //升序排列排序字段
        if(this.getFcategorySort() != null && compare.getFcategorySort() != null){
            i = this.getFcategorySort().compareTo(compare.getFcategorySort());
        }
        //降叙排列修改时间
        if (i == 0 && this.getFmodifyTime() != null && compare.getFmodifyTime() != null ) {
            i = compare.getFmodifyTime().compareTo(this.getFmodifyTime());
        }
        //降叙排列主键id
        if (i == 0 && this.getFcategoryId() != null && compare.getFcategoryId() != null) {
            i = compare.getFcategoryId().compareTo(this.getFcategoryId());
        }
        return i;
    }
}
