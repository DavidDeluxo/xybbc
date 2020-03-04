package com.xingyun.bbc.mallpc.model.vo.excel.sku;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelCollection;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/3/4 11:22
 * @description: TODO
 * @package com.xingyun.bbc.mallpc.model.vo.excel.sku
 */
@Data
public class SkuBatchExportVo {

    @Excel(name = "效期", width = 20, needMerge = true)
    @ApiModelProperty(value = "效期", dataType = "string")
    private String qualityDate;

    @Excel(name = "发货仓", width = 20, needMerge = true)
    @ApiModelProperty(value = "发货仓", dataType = "string")
    private String fwarehouseName;

    @Excel(name = "库存", width = 20, needMerge = true)
    @ApiModelProperty(value = "库存")
    private Long fstockRemianNum;

    @ApiModelProperty("规格")
    @ExcelCollection(name = "规格")
    private List<SkuBatchPackageExportVo> skuBatchPackageExportVoList;
}