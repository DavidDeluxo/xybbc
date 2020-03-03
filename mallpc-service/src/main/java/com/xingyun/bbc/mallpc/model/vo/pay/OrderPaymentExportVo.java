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
public class OrderPaymentExportVo {

    @Excel(name = "下单时间", width = 20, needMerge = true)
    @ApiModelProperty("下单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String fcreateTime;

    @Excel(name = "支付单号", width = 20, needMerge = true)
    @ApiModelProperty(value = "支付单号", dataType = "string")
    private String forderPaymentId;

    @Excel(name = "支付单状态", width = 20, needMerge = true)
    @ApiModelProperty(value = "支付单状态", dataType = "string")
    private String forderStatusStr;

    @Excel(name = "支付单应付总额", width = 20, needMerge = true)
    @ApiModelProperty(value = "支付单应付总额", dataType = "string")
    private String ftotalOrderAmountStr;

    @Excel(name = "支付方式", width = 20, needMerge = true)
    @ApiModelProperty(value = "支付方式", dataType = "string")
    private String forderPayTypeStr;

    @Excel(name = "支付人姓名", width = 20, needMerge = true)
    @ApiModelProperty(value = "支付人姓名", dataType = "string")
    private String fpayerName;

    @Excel(name = "支付人身份证号", width = 20, needMerge = true)
    @ApiModelProperty(value = "支付人身份证号", dataType = "string")
    private String fpayerCardId;

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

    @ApiModelProperty("订单")
    @ExcelCollection(name = "订单")
    private List<OrderExportVo> orderExportVoList;
}