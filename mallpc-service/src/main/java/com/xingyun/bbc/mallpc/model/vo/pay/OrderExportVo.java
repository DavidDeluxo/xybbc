package com.xingyun.bbc.mallpc.model.vo.pay;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelCollection;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2019/12/20 17:55
 * @package com.xingyun.bbc.mallpc.model.vo.pay
 */
@Data
public class OrderExportVo {

    @Excel(name = "行云订单号", width = 20, needMerge = true)
    @ApiModelProperty(value = "行云订单号", dataType = "string")
    private String forderId;

    @Excel(name = "订单状态", width = 20, needMerge = true)
    @ApiModelProperty(value = "订单状态", dataType = "string")
    private String forderStatusStr;

    @ApiModelProperty("商品")
    @ExcelCollection(name = "商品")
    private List<OrderSkuExportVo> orderSkuExportVoList;

    @Excel(name = "订单运费", width = 20, needMerge = true)
    @ApiModelProperty(value = "订单运费", dataType = "string")
    private String ffreightAmountStr;

    @Excel(name = "订单税费", width = 20, needMerge = true)
    @ApiModelProperty(value = "订单税费", dataType = "string")
    private String ftaxAmountStr;

    @Excel(name = "订单优惠金额", width = 20, needMerge = true)
    @ApiModelProperty(value = "订单优惠金额", dataType = "string")
    private String forderDiscountAmountStr;

    @Excel(name = "订单总额", width = 20, needMerge = true)
    @ApiModelProperty(value = "订单总额", dataType = "string")
    private String forderAmountStr;

    @Excel(name = "代购价", width = 20, needMerge = true)
    @ApiModelProperty(value = "代购价", dataType = "string")
    private String fagentAmountStr;

    @Excel(name = "税差", width = 20, needMerge = true)
    @ApiModelProperty(value = "税差", dataType = "string")
    private String ftaxDifferenceStr;

    @Excel(name = "收益", width = 20, needMerge = true)
    @ApiModelProperty(value = "收益", dataType = "string")
    private String fbuyAgentIncomeStr;

    @ApiModelProperty("物流")
    @ExcelCollection(name = "物流")
    private List<OrderExpressExportVo> orderExpressExportVoList;
}