package com.xingyun.bbc.mallpc.model.vo.excel.sku;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelCollection;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/3/4 11:08
 * @description: TODO
 * @package com.xingyun.bbc.mallpc.model.vo.excel.sku
 */
@Data
public class SaleSkuExportVo {

    @Excel(name = "商品名称", width = 20, needMerge = true)
    @ApiModelProperty(value = "商品名称", dataType = "string")
    private String fskuName;

    @Excel(name = "商品描述", width = 20, needMerge = true)
    @ApiModelProperty(value = "商品描述", dataType = "string")
    private String fskuDesc;

    @Excel(name = "商品编码", width = 20, needMerge = true)
    @ApiModelProperty(value = "商品编码", dataType = "string")
    private String fskuCode;

    @Excel(name = "国际条码", width = 20, needMerge = true)
    @ApiModelProperty(value = "国际条码", dataType = "int")
    private String finternationalCode;

    @Excel(name = "品牌", width = 20, needMerge = true)
    @ApiModelProperty(value = "品牌", dataType = "string")
    private String fbrandName;

    @Excel(name = "原产地", width = 20, needMerge = true)
    @ApiModelProperty(value = "原产地", dataType = "string")
    private String fcountryName;

    @Excel(name = "贸易类型", width = 20, needMerge = true)
    @ApiModelProperty(value = "贸易类型", dataType = "string")
    private String ftradeName;

    @Excel(name = "一级分类", width = 20, needMerge = true)
    @ApiModelProperty(value = "一级分类", dataType = "string")
    private String fcategoryName1;

    @Excel(name = "二级分类", width = 20, needMerge = true)
    @ApiModelProperty(value = "二级分类", dataType = "string")
    private String fcategoryName2;

    @Excel(name = "三级分类", width = 20, needMerge = true)
    @ApiModelProperty(value = "三级分类", dataType = "string")
    private String fcategoryName3;

    @Excel(name = "计量单位", width = 20, needMerge = true)
    @ApiModelProperty(value = "计量单位", dataType = "string")
    private String unitName = "件";

    @ApiModelProperty("批次")
    @ExcelCollection(name = "批次")
    private List<SkuBatchExportVo> skuBatchExportVoList;
}