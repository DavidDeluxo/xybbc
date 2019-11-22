package com.xingyun.bbc.mallpc.model.vo.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.xingyun.bbc.mallpc.common.utils.AccountUtil;
import com.xingyun.bbc.mallpc.model.vo.ImageVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @ClassName UserVerifyVO
 * @Description
 * @Author pengaoluo
 * @Date 2019/8/18 17:49
 * @Version 1.0
 */
@Data
public class UserVerifyVO {

    @ApiModelProperty("用户认证ID")
    private Long fuserVerifyId;

    @ApiModelProperty("用户ID")
    private Long fuid;

    @ApiModelProperty("认证状态: 1未认证，2 认证中，3 已认证，4未通过 ")
    private Integer fverifyStatus;

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

    @ApiModelProperty(value = "店铺门头照片", hidden = true)
    private String fshopFront;

    @ApiModelProperty("店铺门头照片")
    private ImageVo fshopFrontImage;

    @ApiModelProperty(value = "店铺实景照片", hidden = true)
    private String fshopInside;

    @ApiModelProperty("店铺实景照片")
    private ImageVo fshopInsideImage;

    @ApiModelProperty("门店地址省份")
    private Long fshopProvinceId;

    @ApiModelProperty("门店地址城市")
    private Long fshopCityId;

    @ApiModelProperty("门店地址区域")
    private Long fshopAreaId;

    @ApiModelProperty("门店详细地址")
    private String fshopAddress;

    @ApiModelProperty("销售平台")
    private String fplatform;

    @ApiModelProperty(value = "月销量", hidden = true)
    private Long fsalesVolume;

    @ApiModelProperty("月销量")
    @JsonSerialize(using = ToStringSerializer.class)
    private java.math.BigDecimal fsalesVolumeShow;

    @ApiModelProperty("用户量")
    private Long fcustomerNum;

    @ApiModelProperty("企业名称")
    private String fcompanyName;

    @ApiModelProperty("营业执照编号")
    private String fbusinessLicenseNo;

    @ApiModelProperty(value = "营业执照照片", hidden = true)
    private String fbusinessLicensePic;

    @ApiModelProperty("营业执照照片")
    private ImageVo fbusinessLicensePicImage;

    @ApiModelProperty("个人姓名")
    private String fname;

    @ApiModelProperty("身份证号码")
    private String fidcardNo;

    @ApiModelProperty(value = "身份证正面照", hidden = true)
    private String fidcardFront;

    @ApiModelProperty("身份证正面照")
    private ImageVo fidcardFrontImage;

    @ApiModelProperty(value = "身份证背面照", hidden = true)
    private String fidcardBack;

    @ApiModelProperty("身份证背面照")
    private ImageVo fidcardBackImage;

    @ApiModelProperty("备注")
    private String fremark;

    @ApiModelProperty("创建时间")
    private java.util.Date fcreateTime;

    @ApiModelProperty("修改时间")
    private java.util.Date fmodifyTime;

    public void setFsalesVolume(Long fsalesVolume) {
        this.fsalesVolume = fsalesVolume;
        this.fsalesVolumeShow = AccountUtil.divideOneHundred(fsalesVolume);
    }

}
