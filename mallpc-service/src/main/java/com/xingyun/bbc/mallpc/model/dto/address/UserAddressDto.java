package com.xingyun.bbc.mallpc.model.dto.address;

import com.xingyun.bbc.mallpc.model.validation.extensions.annotations.NumberRange;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-18
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class UserAddressDto {

    @ApiModelProperty("用户收货地址ID")
    private String fdeliveryUserId;

    @ApiModelProperty("姓名")
    @NotBlank(message = "收件人姓名不能为空")
    private String fdeliveryName;

    @ApiModelProperty("手机号码")
    @NotBlank(message = "手机号码不能为空")
    private String fdeliveryMobile;

    @ApiModelProperty("省份ID")
    @NotNull(message = "省份ID不能为空")
    private Long fdeliveryProvinceId;

    @ApiModelProperty("省份名称")
    @NotBlank(message = "省份名称不能为空")
    private String fdeliveryProvinceName;

    @ApiModelProperty("市ID")
    @NotNull(message = "市ID不能为空")
    private Long fdeliveryCityId;

    @ApiModelProperty("市名称")
    @NotBlank(message = "市名称不能为空")
    private String fdeliveryCityName;

    @ApiModelProperty("区/镇ID")
    @NotNull(message = "区/镇ID不能为空")
    private Long fdeliveryAreaId;

    @ApiModelProperty("区/镇名称")
    @NotBlank(message = "区/镇名称不能为空")
    private String fdeliveryAreaName;

    @ApiModelProperty("详细地址")
    @NotBlank(message = "详细地址不能为空")
    private String fdeliveryAddr;

    @ApiModelProperty("身份证号码")
    private String fdeliveryCardid;

    @ApiModelProperty("身份证正面")
    private String fdeliveryCardUrlFront;

    @ApiModelProperty("身份证反面")
    private String fdeliveryCardUrlBack;

    @ApiModelProperty("是否默认地址(0否, 1是)")
    @NumberRange(values = {0,1},message = "是否为默认地址参数非法")
    private Integer fisDefualt;

}
