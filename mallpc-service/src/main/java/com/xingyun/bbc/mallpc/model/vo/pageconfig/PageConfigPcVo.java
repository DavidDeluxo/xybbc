package com.xingyun.bbc.mallpc.model.vo.pageconfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xingyun.bbc.core.operate.enums.PageConfigPcEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

@Data
public class PageConfigPcVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private Long fconfigId;

    /**
     * 名称
     */
    @ApiModelProperty("名称")
    private String fconfigName;

    /**
     * 排序
     */
    @ApiModelProperty("排序")
    private Long fsortValue;

    /**
     * 图片地址
     */
    @ApiModelProperty("图片地址")
    private String fimgUrl;

    /**
     * 模块类型(0商品分类 1商品品牌 2商品标签 3专题活动 4文章)
     */
    @ApiModelProperty("模块类型")
    private Integer ftype;

    @ApiModelProperty("模块类型")
    private String ftypedesc;

    @JsonIgnore
    @ApiModelProperty("导航栏位置(0Banner配置 1ICON配置 2专题位配置")
    private Integer fposition;


    /**
     * 模块id 类型为1关联商 品分类的(分类id)  类型为2 关联商品品牌的品牌id  类型为3 关联商品标签的标签id
     */
    @ApiModelProperty("模块id")
    private Long frelationId;


    @JsonIgnore
    /** 展示类型(0长期 1固定周期) */
    @ApiModelProperty("展示类型(0长期 1固定周期)")
    private Integer fviewType;

    @JsonIgnore
    /** 固定周期开始时间 */
    @ApiModelProperty("固定周期开始时间")
    private Date fperiodStartTime;

    @JsonIgnore
    /** 固定周期结束时间 */
    @ApiModelProperty("固定周期结束时间")
    private Date fpeiodEndTime;

    /**
     * 分类级别:1 一级分类  2 二级分类  3 三级分类
     */
    @ApiModelProperty("分类级别:1 一级分类  2 二级分类  3 三级分类")
    private Integer fcategoryLevel;

    public void setFtype(Integer ftype) {
        this.ftype = ftype;
        if (Objects.nonNull(this.ftype)) {
            Arrays.stream(PageConfigPcEnum.values()).filter(item -> Integer.valueOf(item.getKey()).equals(ftype)).findFirst().ifPresent(
                    item -> this.ftypedesc = item.getValue()
);
        }
    }
}