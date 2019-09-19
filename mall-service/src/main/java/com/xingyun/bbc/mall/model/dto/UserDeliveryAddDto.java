package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author:lll
 */
@Data
public class UserDeliveryAddDto implements Serializable{


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

 /*   *//** 地址邮编 *//*
    @ApiModelProperty("地址邮编")
    private String fdeliveryPostcode;*/

    /** 身份证号码 */
    @ApiModelProperty("身份证号码")
    private String fdeliveryCardid;

    /** 身份证正面 */
    @ApiModelProperty(" 身份证正面")
    private String fdeliveryCardUrlFront;

    /** 身份证反面 */
    @ApiModelProperty("身份证反面")
    private String fdeliveryCardUrlBack;

/*    *//** 是否删除(0 否 1是) *//*
    @ApiModelProperty("是否删除(0 否 1是)")
    private Integer fisDelete;

    *//** 创建时间 *//*
    @ApiModelProperty("创建时间")
    private Date fcreateTime;

    *//** 更新时间 *//*
    @ApiModelProperty(" 更新时间")
    private Date fmodifyTime;*/


}