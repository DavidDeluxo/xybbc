package com.xingyun.bbc.mallpc.model.vo.pay;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelCollection;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class OrderDetailExportVo {

    @Excel(name = "下单时间", width = 20, needMerge = true)
    @ApiModelProperty("下单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String fcreateTime;

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



    @Excel(name = "收件人姓名", width = 20, needMerge = true)
    @ApiModelProperty(value = "收件人姓名", dataType = "string")
    private String fdeliveryName;

    @Excel(name = "收件人手机", width = 20, needMerge = true)
    @ApiModelProperty(value = "收件人手机", dataType = "string")
    private String fdeliveryMobile;

    @Excel(name = "收件人身份证号码", width = 20, needMerge = true)
    @ApiModelProperty(value = "收货人身份证号码", dataType = "string")
    private String fdeliveryCardid;

    @Excel(name = "省", width = 20, needMerge = true)
    @ApiModelProperty(value = "省", dataType = "string")
    private String fdeliveryProvince;

    @Excel(name = "市", width = 20, needMerge = true)
    @ApiModelProperty(value = "市", dataType = "string")
    private String fdeliveryCity;

    @Excel(name = "区", width = 20, needMerge = true)
    @ApiModelProperty(value = "区", dataType = "string")
    private String fdeliveryArea;

    @Excel(name = "详细地址", width = 20, needMerge = true)
    @ApiModelProperty(value = "详细地址", dataType = "string")
    private String fdeliveryAddr;


    @Excel(name = "支付人姓名", width = 20, needMerge = true)
    @ApiModelProperty(value = "支付人姓名", dataType = "string")
    private String fpayerName;

    @Excel(name = "支付人身份证号", width = 20, needMerge = true)
    @ApiModelProperty(value = "支付人身份证号", dataType = "string")
    private String fpayerCardId;

    @Excel(name = "支付单号", width = 20, needMerge = true)
    @ApiModelProperty(value = "支付单号", dataType = "string")
    private String forderPaymentId;

    @Excel(name = "支付单状态", width = 20, needMerge = true)
    @ApiModelProperty(value = "支付单状态", dataType = "string")
    private String forderPaymentStatusStr;

    @ApiModelProperty("物流")
    @ExcelCollection(name = "物流")
    private List<OrderExpressExportVo> orderExpressExportVoList;

}