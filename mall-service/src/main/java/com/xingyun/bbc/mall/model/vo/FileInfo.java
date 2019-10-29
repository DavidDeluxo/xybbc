package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FileInfo {

    @ApiModelProperty("地址信息文件")
    AddressFileInfoVo addressFileInfo;

}
