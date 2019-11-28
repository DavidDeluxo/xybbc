package com.xingyun.bbc.mallpc.model.dto.address;

import com.xingyun.bbc.mallpc.model.dto.PageDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-18
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
public class UserAddressListDto extends PageDto {

    @ApiModelProperty("收件人姓名")
    private String fdeliveryName;

    @ApiModelProperty("手机号")
    private String fdeliveryMobile;

    @ApiModelProperty("身份证号码")
    private String fdeliveryCardid;

    @ApiModelProperty("是否为默认地址0 不是 1是")
    private Integer isDefault;

}
