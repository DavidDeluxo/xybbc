package com.xingyun.bbc.mallpc.model.vo.aftersale;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel(value = "售后订单列表")
public class AftersaleListVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "售后单号")
    private String forderAftersaleId;

    @ApiModelProperty("商品Id")
    private Long fgoodsId;

    @ApiModelProperty("skuId")
    private Long fskuId;

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

    @ApiModelProperty(value = "售后状态，1待客审、2待采审、3待商审、4待财审、6待退货、7待退款、8已成功 9已撤销、10客服拒绝、11采购拒绝、12供应商拒绝、13财务拒绝、14采购拒绝收货、15逾期回寄")
    private Integer faftersaleStatus;

    @ApiModelProperty(value = "售后申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String fcreateTime;

    @ApiModelProperty(value = "收件人姓名")
    private String fdeliveryName;

    @ApiModelProperty(value = "收件人手机号")
    private String fdeliveryMobile;

    @ApiModelProperty(value = "效期")
    private String fvalidityPeriod;

    @ApiModelProperty(value = "贸易类型")
    private String ftradeType;


}