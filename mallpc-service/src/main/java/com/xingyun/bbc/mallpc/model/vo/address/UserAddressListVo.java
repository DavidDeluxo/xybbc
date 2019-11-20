package com.xingyun.bbc.mallpc.model.vo.address;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-18
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class UserAddressListVo implements Serializable {

    private static final long serialVersionUID = -2385931050753770845L;

    @ApiModelProperty("用户收货地址ID")
    private String fdeliveryUserId;

    @ApiModelProperty("收件人姓名")
    private String fdeliveryName;

    @ApiModelProperty("手机号")
    private String fdeliveryMobile;

    @ApiModelProperty("所在地区")
    private String deliveryArea;

    @ApiModelProperty("详细地址")
    private String fdeliveryAddr;

    @ApiModelProperty("身份证号码")
    private String fdeliveryCardid;

    @ApiModelProperty("身份证正反面是否已传")
    private String isCardUpload;

    @ApiModelProperty("是否为默认收货地址")
    private String isDefualt;
}
