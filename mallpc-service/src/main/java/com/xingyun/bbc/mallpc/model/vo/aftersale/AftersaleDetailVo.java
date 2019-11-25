package com.xingyun.bbc.mallpc.model.vo.aftersale;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(value = "售后订单详情")
public class AftersaleDetailVo  implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "售后单号")
    private String forderAftersaleId;

    @ApiModelProperty(value = "SKU编码")
    private String fskuCode;

    @ApiModelProperty(value = "SKU名称")
    private String fskuName;

    @ApiModelProperty(value = "发货单号")
    private String ftransportOrderId;

    @ApiModelProperty(value = "批次号")
    private String fbatchId;

    @ApiModelProperty(value = "单价")
    private BigDecimal funitPrice;

    @ApiModelProperty(value = "售后总金额")
    private BigDecimal faftersaleTotalAmount;

    @ApiModelProperty(value = "SKU件装数")
    private Long fbatchPackageNum;

    @ApiModelProperty(value = "装数")
    private String fbatchPackageName;

    @ApiModelProperty(value = "SKU图片")
    private String fskuPic;

    @ApiModelProperty(value = "售后申请数量")
    private Integer faftersaleNum;

    @ApiModelProperty(value = "售后申请数量展示")
    private String faftersaleNumShow;

    @ApiModelProperty(value = "售后状态1待客服审核 2待采购审核 3待仓库审核 4待财务审核 5已拒绝 6待退货 7待退款 8已成功 9已撤销")
    private Integer faftersaleStatus;

    @ApiModelProperty(value = "售后原因类型 1客户申请 2供应商无法发货 3供应商漏发 4供应商延期发货 5供应商商品发错 6商品质量问题 7商品运输破损")
    private Integer faftersaleReason;

    @ApiModelProperty(value = "售后类型 1 退款 2 退款退货")
    private Integer faftersaleType;

    @ApiModelProperty(value = "回寄收件地址省")
    private String fdeliveryProvince;

    @ApiModelProperty(value = "回寄收件地址市")
    private String fdeliveryCity;

    @ApiModelProperty(value = "回寄收件地址区")
    private String fdeliveryArea;

    @ApiModelProperty(value = "回寄收件详细地址")
    private String fdeliveryAddr;

    @ApiModelProperty(value = "回寄收件人")
    private String fdeliveryName;

    @ApiModelProperty(value = "回寄收件人电话")
    private String fdeliveryMobile;

    @ApiModelProperty(value = "回寄状态 1 未签收 2已签收")
    private Integer fbackStatus;

    @ApiModelProperty(value = "退货时间")
    private Date freGoodsTime;

    @ApiModelProperty(value = "退款时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm")
    private Date frefundTime;

    @ApiModelProperty(value = "效期")
    private String fvalidityPeriod;

}
