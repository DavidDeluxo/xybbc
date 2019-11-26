package com.xingyun.bbc.mallpc.model.vo.aftersale;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "售后用户回寄物流信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AftersaleBackVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "售后单号")
    private String forderAftersaleId;

    @ApiModelProperty(value = "物流公司id")
    private Long flogisticsCompanyId;

    @ApiModelProperty(value = "回寄状态 1 未签收 2已签收")
    private Integer fbackStatus;

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

    @ApiModelProperty(value = "用户回寄物流公司名称")
    private String flogisticsCompanyName;

    @ApiModelProperty(value = "用户回寄物流单号")
    private String fbackLogisticsOrder;

    @ApiModelProperty(value = "用户回寄联系电话")
    private String fbackMobile;

    @ApiModelProperty(value = "用户回寄问题描述")
    private String fbackRemark;

    @ApiModelProperty(value = "用户回寄凭证")
    private List<String> fuserAftersalePic;


}
