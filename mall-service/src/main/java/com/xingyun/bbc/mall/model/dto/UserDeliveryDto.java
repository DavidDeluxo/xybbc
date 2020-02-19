package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author:lll
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class UserDeliveryDto extends PageDto{


    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long fuid;

    /** 省份ID */
    @ApiModelProperty("省份ID")
    private Long fdeliveryProvinceId;

    /** 省份名称 */
    @ApiModelProperty("省份名称")
    private String fdeliveryProvinceName;

    /** 市ID */
    @ApiModelProperty("市ID")
    private Long fdeliveryCityId;

    /** 市名称 */
    @ApiModelProperty("市名称")
    private String fdeliveryCityName;

    /** 区/镇ID */
    @ApiModelProperty("区/镇ID")
    private Long fdeliveryAreaId;

    /** 区/镇名称 */
    @ApiModelProperty("区/镇名称")
    private String fdeliveryAreaName;

    /** 是否默认地址(0否, 1是) */
    @ApiModelProperty("是否默认地址(0否, 1是)")
    private Integer fisDefualt;

    /** 手机号码 */
    @ApiModelProperty("手机号码")
    private String fdeliveryMobile;

    /** 姓名 */
    @ApiModelProperty("姓名")
    private String fdeliveryName;

    /** 详细地址 */
    @ApiModelProperty("详细地址")
    private String fdeliveryAddr;

    /** 身份证号码 */
    @ApiModelProperty("身份证号码")
    private String fdeliveryCardid;

    /** 身份证正面 */
    @ApiModelProperty(" 身份证正面")
    private String fdeliveryCardUrlFront;

    /** 身份证反面 */
    @ApiModelProperty("身份证反面")
    private String fdeliveryCardUrlBack;

    /** 用户收货地址IDS */
    @ApiModelProperty("用户IDS")
    private String fuids;

    /** 用户收货地址查询关键词 */
    @ApiModelProperty("地址查询关键词")
    private String keyWord;
}