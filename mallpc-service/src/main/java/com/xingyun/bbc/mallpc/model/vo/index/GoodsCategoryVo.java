package com.xingyun.bbc.mallpc.model.vo.index;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-25
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class GoodsCategoryVo implements Serializable ,Comparable<GoodsCategoryVo>{

    private static final long serialVersionUID = -3849493625381158958L;

    @ApiModelProperty(value = "商品分类Id", example = "1")
    private Long fcategoryId;

    @ApiModelProperty(value = "商品分类名称", example = "奶粉")
    private String fcategoryName;

    @ApiModelProperty(value = "上级分类Id", example = "0")
    private Long fparentCategoryId;

    @ApiModelProperty(value = "分类排序", example = "2")
    private Integer fcategorySort;

    @ApiModelProperty(value = "分类编码", example = "nike-001")
    private String fcategoryCode;

    @ApiModelProperty(value = "分类描述", example = "这是描述")
    private String fcategoryDesc;

    @ApiModelProperty(value = "分类层级 1 2 3")
    private Integer flevel;

    @ApiModelProperty(value = "创建时间", example = "2019-08-17 19:05:32")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fcreateTime;

    @ApiModelProperty(value = "修改时间", example = "2019-08-17 19:05:32")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fmodifyTime;

    @ApiModelProperty(value = "分类图片绝对路径")
    private String imageUrl;

    @ApiModelProperty(value = "下级分类集合")
    private Set<GoodsCategoryVo> children = new TreeSet<>();

    @Override
    public int compareTo(GoodsCategoryVo compare) {
        int i = this.getFcategorySort().compareTo(compare.getFcategorySort());
        if (i == 0) {
            i = compare.getFmodifyTime().compareTo(this.getFmodifyTime());
        }
        if (i==0){
            i = this.getFcategoryId().compareTo(compare.getFcategoryId());
        }
        return i;
    }
}
