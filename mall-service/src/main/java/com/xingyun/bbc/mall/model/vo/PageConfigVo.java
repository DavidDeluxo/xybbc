package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author:lll
 */
@Data
public class PageConfigVo {


    /** 名称 */
    @ApiModelProperty(value = "名称")
    private String fconfigName;

    /** 排序 */
    @ApiModelProperty(value = "排序")
    private Long fsortValue;

    /** 图片地址 */
    @ApiModelProperty(value = "图片地址")
    private String fimgUrl;

    /** 模块类型(0商品分类 1商品品牌 2商品标签 3专题活动 4文章) */
    @ApiModelProperty(value = "模块类型(0商品分类 1商品品牌 2商品标签 3专题活动 4文章)")
    private Integer ftype;
	
	/** 导航栏位置(0Banner配置 1ICON配置 2专题位配置*/
    @ApiModelProperty(value = "导航栏位置(0Banner配置 1ICON配置 2专题位配置")
    private Integer fposition;
	
	/** 排版对应位置(排版对应的位置左一至左五对应值1-5)*/
    @ApiModelProperty(value = "排版对应位置(排版对应的位置左一至左五对应值1-5)")
	private Integer flocation;

    /** 模块id 类型为1关联商 品分类的(分类id)  类型为2 关联商品品牌的品牌id  类型为3 关联商品标签的标签id */
    @ApiModelProperty(value = "模块id 类型为1关联商 品分类的(分类id)  类型为2 关联商品品牌的品牌id  类型为3 关联商品标签的标签id")
    private Long frelationId;

    /** 展示类型(0长期 1固定周期) */
    @ApiModelProperty(value = "展示类型(0长期 1固定周期)")
    private Integer fviewType;

    /** 固定周期开始时间 */
    @ApiModelProperty(value = "固定周期开始时间 ")
    private Date fperiodStartTime;

   /** 固定周期结束时间 */
    @ApiModelProperty(value = "固定周期结束时间")
    private Date fpeiodEndTime;

   /** 后端接口地址 */
    @ApiModelProperty(value = "后端接口地址")
    private String fredirectUrl;
}