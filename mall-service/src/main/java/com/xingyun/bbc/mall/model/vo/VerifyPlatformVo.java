package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VerifyPlatformVo {
    @ApiModelProperty("平台名称")
    private String fplatformName;

    @ApiModelProperty("平台ID")
    private Integer fplatforId;
}
