package com.xingyun.bbc.mallpc.model.vo.index;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SpecialTopicVo implements Serializable {
    private static final long serialVersionUID = 8421252260683429894L;
    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    private String fconfigName;

    /**
     * 排序
     */
    @ApiModelProperty(value = "排序")
    private Long fsortValue;

    /**
     * 图片地址
     */
    @ApiModelProperty(value = "图片地址")
    private String fimgUrl;

    /**
     * 模块类型(0商品分类 1商品品牌 2商品标签 3专题活动 4文章 5SKU 6领券中心)
     */
    @ApiModelProperty(value = "模块类型(0商品分类 1商品品牌 2商品标签 3专题活动 4文章 5SKU 6领券中心)")
    private Integer ftype;

    /**
     * 模块id
     */
    @ApiModelProperty(value = "模块id ")
    private Long frelationId;

    /**分类级别:1 一级分类  2 二级分类  3 三级分类**/
    @ApiModelProperty(value = "分类级别:1 一级分类  2 二级分类  3 三级分类")
    private Integer fcategoryLevel;

    /**
     * 展示类型(0长期 1固定周期)
     */
    @ApiModelProperty(value = "展示类型(0长期 1固定周期)")
    private Integer fviewType;

    /**
     * 固定周期开始时间
     */
    @ApiModelProperty(value = "固定周期开始时间 ")
    private Date fperiodStartTime;

    /**
     * 固定周期结束时间
     */
    @ApiModelProperty(value = "固定周期结束时间")
    private Date fpeiodEndTime;
}
