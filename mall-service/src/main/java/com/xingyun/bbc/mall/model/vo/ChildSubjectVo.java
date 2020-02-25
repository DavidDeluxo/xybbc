package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ChildSubjectVo {

    /**
     * 楼层id
     */
    @ApiModelProperty("楼层id")
    private Long fsubjectFloorId;

    /**
     * 父专题id
     */
    @ApiModelProperty("父专题id")
    private Long fsubjectParentId;

    /**
     * 所属专题id
     */
    @ApiModelProperty("专题id")
    private Long fsubjectId;

    /**
     * 楼层排序
     */
    @ApiModelProperty("排序")
    private Integer fsubjectFloorSort;

    /**
     * 展示样式(移动端) 1 单列单大图 2 单列单小图 3 单列双小图 4 单列三小图
     */
    @ApiModelProperty("展示样式(移动端) 1 单列单大图 2 单列单小图 3 单列双小图 4 单列三小图")
    private Integer fsubjectMobileLayout;

    /**
     * 楼层内容 1 优惠券专题 2 活动专题
     */
    @ApiModelProperty("内容 1 优惠券专题 2 活动专题")
    private Integer fsubjectFloorContentType;

    /****************************************************************/

    /**
     * 专题名称
     */
    @ApiModelProperty("专题名称")
    private String fsubjectName;

    /**
     * 专题描述
     */
    @ApiModelProperty("专题描述")
    private String fsubjectDescription;

    /**
     * 专题状态 1 待开始 2 进行中 3 已结束
     */
    @ApiModelProperty("专题状态 1 待开始 2 进行中 3 已结束")
    private Integer fsubjectStatus;

    /**
     * 专题背景色
     */
    @ApiModelProperty("专题背景色")
    private String fsubjectBackgroundColor;

    /**
     * 专题名称背景图(移动端)
     */
    @ApiModelProperty("专题名称背景图(移动端)")
    private String fsubjectMobileBackgroundPic;

    /**
     * 专题图片(移动端)
     */
    @ApiModelProperty("专题图片(移动端)")
    private String fsubjectMobilePic;

    @ApiModelProperty("商品")
    private List<SearchItemVo> searchItemVoList;
}
