package com.xingyun.bbc.mallpc.model.dto.detail;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("商品详情")
public class GoodsDetailMallDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("fuid")
    private Long fuid;

    @ApiModelProperty("关联商品Id")
    private Long fgoodsId;

    @ApiModelProperty("商品skuId")
    private Long fskuId;

    @ApiModelProperty("批次id")
    private String fsupplierSkuBatchId;

    @ApiModelProperty("包装规格Id")
    private Long fbatchPackageId;

    @ApiModelProperty("购买数量")
    private Long fnum;

    @ApiModelProperty("规格数量")
    private Long fbatchPackageNum;

    @ApiModelProperty("市ID")
    private Long fdeliveryCityId;

    @ApiModelProperty("用户认证类型")
    private Integer foperateType;

    @ApiModelProperty("用户认证状态")
    private Integer fverifyStatus;

    @ApiModelProperty("sku下是否支持折扣")
    private Integer fskuDiscount;

    @ApiModelProperty("sku下用户认证类型是否支持折扣")
    private Integer fskuUserDiscount;

    @ApiModelProperty("sku税率")
    private BigDecimal fskuTaxRate;


}
