package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
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
    @NotNull
    private Long fdeliveryProvinceId;

    /** 省份名称 */
    @ApiModelProperty("省份名称")
    @NotNull
    private String fdeliveryProvinceName;

    /** 市ID */
    @ApiModelProperty("市ID")
    @NotNull
    private Long fdeliveryCityId;

    /** 市名称 */
    @ApiModelProperty("市名称")
    @NotNull
    private String fdeliveryCityName;

    /** 区/镇ID */
    @ApiModelProperty("区/镇ID")
    @NotNull
    private Long fdeliveryAreaId;

    /** 区/镇名称 */
    @NotNull
    @ApiModelProperty("区/镇名称")
    private String fdeliveryAreaName;

    /** 是否默认地址(0否, 1是) */
    @ApiModelProperty("是否默认地址(0否, 1是)")
    private Integer fisDefualt;

    /** 手机号码 */
    @ApiModelProperty("手机号码")
    @NotNull
    private String fdeliveryMobile;

    /** 姓名 */
    @ApiModelProperty("姓名")
    @NotNull
    private String fdeliveryName;

    /** 详细地址 */
    @ApiModelProperty("详细地址")
    @NotNull
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




}