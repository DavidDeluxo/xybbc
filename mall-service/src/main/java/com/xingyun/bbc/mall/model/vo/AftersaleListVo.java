package com.xingyun.bbc.mall.model.vo;

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

    @ApiModelProperty(value = "SKU编码")
    private String fskuCode;

    @ApiModelProperty(value = "SKU名称")
    private String fskuName;

    @ApiModelProperty(value = "单价")
    private BigDecimal funitPrice;

    @ApiModelProperty(value = "SKU件装数")
    private Long fbatchPackageNum;

    @ApiModelProperty(value = "装数")
    private String fbatchPackageName;

    @ApiModelProperty(value = "SKU图片")
    private String fskuPic;

    @ApiModelProperty(value = "售后申请数量")
    private Integer faftersaleNum;

    @ApiModelProperty(value = "售后状态1待客服审核 2待采购审核 3待仓库审核 4待财务审核 5已拒绝 6待退货 7待退款 8已成功 9已撤销")
    private Integer faftersaleStatus;
}