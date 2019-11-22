package com.xingyun.bbc.mall.model.dto;


import com.xingyun.bbc.common.elasticsearch.config.autobuild.BuildPolicy;
import com.xingyun.bbc.common.elasticsearch.config.autobuild.EsMark;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel("商品搜索条件")
public class SearchItemDto {

    {
        this.pageIndex = 1;
        this.pageSize = 10;
        this.fuserTypeId = "1";
        this.fskuStatus = 1;
        this.isLogin = false;
        this.fisDeleted = 0;
        fuid = 1;
    }

    @ApiModelProperty("页大小")
    @EsMark(policy = BuildPolicy.PAGE_SIZE)
    private Integer pageSize;

    @ApiModelProperty("页码")
    @EsMark(policy = BuildPolicy.PAGE_INDEX)
    private Integer pageIndex;

    @ApiModelProperty("关键词")
    @EsMark(policy = BuildPolicy.MATCH_TEXT, field = "fsku_name")
    private String searchFullText;

    @ApiModelProperty("三级类目id")
    @EsMark(policy = BuildPolicy.MULTI_OR_MUST, field = "fcategory_id3")
    private List<Integer> fcategoryId;

    @ApiModelProperty(value = "二级类目id")
    @EsMark(policy = BuildPolicy.MULTI_OR_MUST, field = "fcategory_id2")
    private List<Integer> fcategoryIdL2;

    @ApiModelProperty(value = "一级类目id")
    @EsMark(policy = BuildPolicy.MULTI_OR_MUST, field = "fcategory_id1")
    private List<Integer> fcategoryIdL1;

    @ApiModelProperty("贸易类型id")
    @EsMark(policy = BuildPolicy.MULTI_OR_MUST, field = "ftrade_id")
    private List<Integer> ftradeId;

    @ApiModelProperty("品牌id")
    @EsMark(policy = BuildPolicy.MULTI_OR_MUST, field = "fbrand_id")
    private List<Integer> fbrandId;

    @ApiModelProperty("原产地id")
    @EsMark(policy = BuildPolicy.MULTI_OR_MUST, field = "forigin_id")
    private List<Integer> foriginId;

    @ApiModelProperty("最低价格")
    private BigDecimal fpriceStart;

    @ApiModelProperty("最高价格")
    private BigDecimal fpriceEnd;

    /**
     * 将你需要的字段名以sourceInclude为入参，可以自定义接口返回字段
     */
    @ApiModelProperty(value = "自定义接口返回字段", hidden = true)
    @EsMark(policy = BuildPolicy.SOUCE_INCLUDE)
    private String sourceIncludes;

    @ApiModelProperty(value = "用户类型", hidden = true)
    private String fuserTypeId;

    @ApiModelProperty("仅看有货 0 否 1 是")
    private Integer isStockNotEmpty;

    @ApiModelProperty("按销量排序 升序 'asc' 降序 'desc'")
    @EsMark(policy = BuildPolicy.SORT, field = "fsell_total")
    private String sellAmountOrderBy;

    @ApiModelProperty("按价格排序 升序 'asc' 降序 'desc'")
    private String priceOrderBy;

    @EsMark(policy = BuildPolicy.MUST, field = "fsku_status")
    @ApiModelProperty(value = "sku状态(1.已上架 2.已下架 3.待上架 4.新增)", hidden = true)
    private Integer fskuStatus;

    @ApiModelProperty("关联商品标签Id")
    @EsMark(policy = BuildPolicy.MULTI_OR_MUST, field = "flabel_id")
    private List<Integer> flabelId;

    @ApiModelProperty(value="用户Id", hidden=true)
    private Integer fuid;

    @ApiModelProperty(value="属性值id")
    private List<Integer> fattributeItemId;

    @ApiModelProperty(value = "用户是否登录", hidden = true)
    private Boolean isLogin;

    @EsMark(policy = BuildPolicy.MUST, field = "fis_delete")
    @ApiModelProperty(value = "是否删除", hidden = true)
    private Integer fisDeleted;

    @EsMark(policy = BuildPolicy.MULTI_OR_MUST, field = "fsku_id")
    @ApiModelProperty(value = "sku_id", hidden = true)
    private List<Long> fskuIds;

    @EsMark(policy = BuildPolicy.MUST, field = "fcoupon_ids")
    @ApiModelProperty("优惠券ID")
    private Long couponId;

    @EsMark(policy = BuildPolicy.MUST_KEY_WORD, field = "fsku_code")
    @ApiModelProperty("商品编码")
    private String fskuCode;



    //----------------------------------------------
    //   banner icon 专题位 配置分类id时使用, 用来区分分类id等级
    @ApiModelProperty(value = "通用类目id")
    private List<Integer> fUnicategoryIds;

    @ApiModelProperty(value = "类目等级")
    private Integer fcategoryLevel;

}
