package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VerifyCategoryVo {
    @ApiModelProperty("类目名称")
    private String fcategoryName;

    @ApiModelProperty("类目ID")
    private Integer fcategoryId;
}
