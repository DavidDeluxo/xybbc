package com.xingyun.bbc.mallpc.model.vo.excel.sku;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/3/4 11:28
 * @description: TODO
 * @package com.xingyun.bbc.mallpc.model.vo.excel.sku
 */
@Data
public class SkuBatchPackageExportVo {

    @Excel(name = "件装数", width = 20, needMerge = true)
    @ApiModelProperty(value = "件装数")
    private Long fbatchPackageNum;

    @Excel(name = "会员价", width = 20, needMerge = true)
    @ApiModelProperty(value = "会员价", dataType = "string")
    private String fbatchPackagePrice;

    @Excel(name = "最少起拍量", width = 20, needMerge = true)
    @ApiModelProperty(value = "最少起拍量")
    private Long fbatchStartNum;


}