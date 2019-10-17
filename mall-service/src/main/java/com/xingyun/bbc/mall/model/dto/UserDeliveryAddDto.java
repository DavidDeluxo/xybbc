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
    @NotNull(message = "省份不能为空")
    private Long fdeliveryProvinceId;

    /** 省份名称 */
    @ApiModelProperty("省份名称")
    @NotNull(message = "省份名称不能为空")
    private String fdeliveryProvinceName;

    /** 市ID */
    @ApiModelProperty("市ID")
    @NotNull(message = "市不能为空")
    private Long fdeliveryCityId;

    /** 市名称 */
    @ApiModelProperty("市名称")
    @NotNull(message = "市名称不能为空")
    private String fdeliveryCityName;

    /** 区/镇ID */
    @ApiModelProperty("区/镇ID")
    @NotNull(message = "区/镇不能为空")
    private Long fdeliveryAreaId;

    /** 区/镇名称 */
    @NotNull(message = "区/镇名称不能为空")
    @ApiModelProperty("区/镇名称")
    private String fdeliveryAreaName;

    /** 是否默认地址(0否, 1是) */
    @ApiModelProperty("是否默认地址(0否, 1是)")
    private Integer fisDefualt;

    /** 手机号码 */
    @ApiModelProperty("手机号码")
    @NotNull(message = "手机号码不能为空")
    private String fdeliveryMobile;

    /** 姓名 */
    @ApiModelProperty("姓名")
    @NotNull(message = "姓名不能为空")
    private String fdeliveryName;

    /** 详细地址 */
    @ApiModelProperty("详细地址")
    @NotNull(message = "详细地址不能为空")
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