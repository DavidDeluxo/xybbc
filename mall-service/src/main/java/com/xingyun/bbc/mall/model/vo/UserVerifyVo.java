package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class UserVerifyVo {
    @ApiModelProperty("认证类型：1实体门店，2网络店铺，3网络平台，4批采企业，5微商代购")
    private Integer foperateType;

    @ApiModelProperty("用户状态：1未认证，2 认证中，3 已认证，4未通过 ")
    private Integer fverifyStatus;

    @ApiModelProperty("用户认证ID")
    private Long fuserVerifyId;

    @ApiModelProperty("用户ID")
    private Long fuid;

    @ApiModelProperty("经营方式")
    private String foperateMethod;

    @ApiModelProperty("店铺名称")
    private String fshopName;

    @ApiModelProperty("店铺网址")
    private String fshopWeb;

    @ApiModelProperty("感兴趣的类目")
    private String finterestItem;

    @ApiModelProperty("经营品类")
    private String fcategory;

    @ApiModelProperty("店铺门头照片")
    private String fshopFront;

    @ApiModelProperty("店铺实景照片")
    private String fshopInside;

    @ApiModelProperty("地址省份")
    private Long fshopProvinceId;

    @ApiModelProperty("地址省份名称")
    private String fshopProvinceName;

    @ApiModelProperty("地址城市")
    private Long fshopCityId;

    @ApiModelProperty("地址城市名称")
    private String fshopCityName;

    @ApiModelProperty("地址区域")
    private Long fshopAreaId;

    @ApiModelProperty("地址区域名称")
    private String fshopAreaName;

    @ApiModelProperty("详细地址")
    private String fshopAddress;

    @ApiModelProperty("销售平台")
    private Long fpaltformId;

    @ApiModelProperty("销售平台名称")
    private String fpaltformName;

    @ApiModelProperty("月销量/分")
    private Long fsalesVolume;

    @ApiModelProperty("月销量/万元")
    private String salesVolume;

    @ApiModelProperty("用户量")
    private Long fcustomerNum;

    @ApiModelProperty("企业名称")
    private String fcompanyName;

    @ApiModelProperty("营业执照编号")
    private String fbusinessLicenseNo;

    @ApiModelProperty("营业执照照片")
    private String fbusinessLicensePic;

    @ApiModelProperty("个人姓名")
    private String fname;

    @ApiModelProperty("身份证号码")
    private String fidcardNo;

    @ApiModelProperty("身份证正面照")
    private String fidcardFront;

    @ApiModelProperty("身份证背面照")
    private String fidcardBack;

    @ApiModelProperty("备注")
    private String fremark;

    @ApiModelProperty("创建时间")
    private Date fcreateTime;

    @ApiModelProperty("修改时间")
    private Date fmodifyTime;
}
