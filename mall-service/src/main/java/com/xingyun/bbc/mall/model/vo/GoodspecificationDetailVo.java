package com.xingyun.bbc.mall.model.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel("商品各种规格详情")
public class GoodspecificationDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品sku主键")
    private Long fskuId;

    @ApiModelProperty(value = "商品sku编号")
    private String fskuCode;

    @ApiModelProperty(value = "sku规格值")
    private String fskuSpecValue;

    @ApiModelProperty(value = "批次id")
    private String fskuBatchId;

    @ApiModelProperty(value = "批次有效期--商品保质期")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date fqualityEndDate;

    @ApiModelProperty(value = "包装规格自增主键")
    private Long fbatchPackageId;

    @ApiModelProperty(value = "包装规格值")
    private Long fbatchPackageNum;

    @ApiModelProperty(value = "起发数")
    private Long fbatchStartNum;


}
