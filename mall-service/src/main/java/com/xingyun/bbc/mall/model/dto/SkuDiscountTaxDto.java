package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("sku税率--是否打折")
public class SkuDiscountTaxDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal fskuTaxRate;

    private Integer fisUserTypeDiscount;

}
