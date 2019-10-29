package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AddressFileInfoVo {

    @ApiModelProperty("文件版本号")
    private String fileVersion;

    @ApiModelProperty("文件地址")
    private String fileAddress;

    @ApiModelProperty("文件名")
    private String fileName;

}
