package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "售后用户回寄物流信息")
public class AftersaleBackVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "售后单号")
    private String forderAftersaleId;

    @ApiModelProperty(value = "物流公司id")
    private Long flogisticsCompanyId;

    @ApiModelProperty(value = "物流公司名称")
    private String flogisticsCompanyName;

    @ApiModelProperty(value = "物流单号")
    private String fbackLogisticsOrder;

    @ApiModelProperty(value = "用户回寄联系电话")
    private String fbackMobile;

    @ApiModelProperty(value = "用户回寄问题描述")
    private String fbackRemark;

    @ApiModelProperty(value = "用户回寄凭证--多个逗号分割")
    private String fpicStr;
}
