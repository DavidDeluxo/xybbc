package com.xingyun.bbc.mallpc.model.vo.index;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author:chenxiang
 */
@Data
public class BannerVo implements Serializable{

    private static final long serialVersionUID = 3583341780396123029L;
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
}