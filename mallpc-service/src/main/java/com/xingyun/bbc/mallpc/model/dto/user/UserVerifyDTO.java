package com.xingyun.bbc.mallpc.model.dto.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.xingyun.bbc.mallpc.common.constants.MallPcConstants;
import com.xingyun.bbc.mallpc.common.utils.AccountUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * @ClassName UserVerifyDTO
 * @Description
 * @Author pengaoluo
 * @Date 2019/8/18 14:24
 * @Version 1.0
 */
@ApiModel
@Data
public class UserVerifyDTO {

    @ApiModelProperty("认证类型：1实体门店，2网络店铺，3网络平台，4批采企业，5微商代购")
    @NotNull(message = "认证类型不能为空")
    @Min(value = 1, message = "认证类型应为1到5的整数")
    @Max(value = 5, message = "认证类型应为1到5的整数")
    private Integer foperateType;

    @ApiModelProperty("用户认证ID")
    private Long fuserVerifyId;

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

    @ApiModelProperty(value = "月销售额", hidden = true)
    private Long fsalesVolume;

    @ApiModelProperty("月销售额")
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal fsalesVolumeShow;

    @ApiModelProperty("企业名称")
    private String fcompanyName;

    @ApiModelProperty("营业执照编号")
    private String fbusinessLicenseNo;

    @ApiModelProperty("营业执照照片")
    private String fbusinessLicensePic;

    @ApiModelProperty("身份证号码")
    @Pattern(regexp = MallPcConstants.IDCARD_REGEXP, message = "身份证号码不正确")
    private String fidcardNo;

    @ApiModelProperty("身份证正面照")
    private String fidcardFront;

    @ApiModelProperty("身份证背面照")
    private String fidcardBack;

    public void setFsalesVolumeShow(BigDecimal fsalesVolumeShow) {
        this.fsalesVolumeShow = fsalesVolumeShow;
        this.fsalesVolume = AccountUtil.multiplyOneHundred(fsalesVolumeShow);
    }

    public void setFbusinessLicenseNo(String fbusinessLicenseNo) {
        this.fbusinessLicenseNo = StringUtils.trimWhitespace(fbusinessLicenseNo);
    }
}
