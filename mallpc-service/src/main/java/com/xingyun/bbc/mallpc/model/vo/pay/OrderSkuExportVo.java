package com.xingyun.bbc.mallpc.model.vo.pay;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2019/12/20 18:34
 * @package com.xingyun.bbc.mallpc.model.vo.pay
 */
@Data
public class OrderSkuExportVo {

    @Excel(name = "商品名称", width = 20, needMerge = true)
    @ApiModelProperty(value = "商品名称", dataType = "string")
    private String fskuName;

    @Excel(name = "商品类型", width = 20, needMerge = true)
    @ApiModelProperty(value = "商品类型", dataType = "string")
    private String ftradeTypeStr;

    @Excel(name = "商品编码", width = 20, needMerge = true)
    @ApiModelProperty(value = "商品编码", dataType = "string")
    private String fskuCode;

    @Excel(name = "下单数量", width = 20, needMerge = true)
    @ApiModelProperty(value = "下单数量", dataType = "int")
    private Integer fskuNum;

    @Excel(name = "商品单价", width = 20, needMerge = true)
    @ApiModelProperty(value = "商品单价", dataType = "string")
    private String fskuPriceStr;

    @Excel(name = "商品金额", width = 20, needMerge = true)
    @ApiModelProperty(value = "商品金额", dataType = "string")
    private String fskuAmountStr;

}