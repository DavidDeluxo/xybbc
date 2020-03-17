package com.xingyun.bbc.mallpc.model.vo.subject;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/1/13 10:20
 * @description: 专题vo
 * @package com.xingyun.bbc.mall.model.vo
 */
@Data
public class SubjectVo {

    /**
     * 专题id
     */
    @ApiModelProperty("专题id")
    private Long fsubjectId;

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

    /**
     * 父专题id
     */
    @ApiModelProperty("父专题id")
    private Long fsubjectParentId;

    /**
     * 专题内容类型 1 商品 2 专题活动 3 优惠券活动
     */
    @ApiModelProperty("专题内容类型 1 商品 2 专题活动 3 优惠券活动")
    private Integer fsubjectContentType;

    /**
     * 专题楼层展示样式(移动端) 1 单列单大图 2 单列单小图 3 单列双小图 4 单列三小图
     */
    @ApiModelProperty("专题楼层展示样式(移动端) 1 单列单大图 2 单列单小图 3 单列双小图 4 单列三小图")
    private Integer fsubjectMobileLayout;

    @ApiModelProperty("优惠券id")
    private Long fcouponId;
}