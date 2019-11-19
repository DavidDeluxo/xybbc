package com.xingyun.bbc.mallpc.model.vo.address;

import com.xingyun.bbc.mallpc.model.vo.ImageVo;
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
public class UserAddressDetailsVo implements Serializable {

    private static final long serialVersionUID = -8321483488618835056L;

    @ApiModelProperty("姓名")
    private String fdeliveryName;

    @ApiModelProperty("手机号码")
    private String fdeliveryMobile;

    @ApiModelProperty("所在地区")
    private String deliveryArea;

    @ApiModelProperty("省份ID")
    private Long fdeliveryProvinceId;

    @ApiModelProperty("市ID")
    private Long fdeliveryCityId;

    @ApiModelProperty("区/镇ID")
    private Long fdeliveryAreaId;

    @ApiModelProperty("详细地址")
    private String fdeliveryAddr;

    @ApiModelProperty("身份证号码")
    private String fdeliveryCardid;

    @ApiModelProperty("身份证正面")
    private ImageVo cardUrlFront;

    @ApiModelProperty("身份证反面")
    private ImageVo cardUrlBack;

    @ApiModelProperty("是否默认地址(0否, 1是)")
    private String fisDefualt;


}
