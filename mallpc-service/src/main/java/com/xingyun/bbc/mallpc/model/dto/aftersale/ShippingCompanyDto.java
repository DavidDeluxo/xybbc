package com.xingyun.bbc.mallpc.model.dto.aftersale;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ShippingCompanyDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    @ApiModelProperty(value = "ID")
    private Long fshippingCompanyId;

    /** 名称 */
    @ApiModelProperty(value = "名称")
    private String fshippingName;

    /** 缩写 */
    @ApiModelProperty(value = "缩写")
    private String fshortName;

    /** 编号 */
    @ApiModelProperty(value = "编号")
    private String fshippingCode;
}
