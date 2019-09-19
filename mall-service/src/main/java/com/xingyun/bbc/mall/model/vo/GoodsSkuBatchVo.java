package com.xingyun.bbc.mall.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel("商品批次")
public class GoodsSkuBatchVo implements Serializable{

    private static final long serialVersionUID = 1L;
	
    /** 自增主键 */
    @ApiModelProperty(value = "批次id")
    private Long fskuBatchId;

    /** sku批次号 */
    @ApiModelProperty(value = "sku批次号")
    private String fsupplierSkuBatchId;

    /** 批次上架排序 */
    @ApiModelProperty(value = "批次上架排序")
    private Integer fbatchPutwaySort;

    /** 关联sku Id */
    @ApiModelProperty(value = "关联sku Id")
    private Long fskuId;

    /** 关联sku编码 */
    @ApiModelProperty(value = "关联sku编码")
    private String fskuCode;

    /** sku名称 */
    @ApiModelProperty(value = "sku名称")
    private String fskuName;

    /** sku国际条码 */
    @ApiModelProperty(value = "sku国际条码")
    private String fskuInternationalCode;

    /** 关联贸易类型Id */
    @ApiModelProperty(value = "关联贸易类型Id")
    private Long ftradeId;

    /** 关联供应商Id */
    @ApiModelProperty(value = "关联供应商Id")
    private Long fsupplierId;

    /** 供应商简称 */
    @ApiModelProperty(value = "供应商简称")
    private String fsupplierAbbreviation;

    /** 供应商发货仓ID */
    @ApiModelProperty(value = "供应商发货仓ID")
    private Long fsupplierWarehouseId;

    /** 仓库区域名称 */
    @ApiModelProperty(value = "仓库区域名称")
    private String fwarehouseName;

    /** 库存预警 */
    @ApiModelProperty(value = "库存预警")
    private Long fstockWarningNum;

    /** 出单时效 */
    @ApiModelProperty(value = "出单时效")
    private Long forderDeliveryTime;

    /** 商品类型  (1、新包装； 2、旧包装 ；3、新旧包装随机 */
    @ApiModelProperty(value = "1、新包装； 2、旧包装 ；3、新旧包装随机")
    private Integer fgoodsPackType;

    /** 关联运费模板Id */
    @ApiModelProperty(value = "关联运费模板Id")
    private Long ffreightId;

    /** 销量 */
    @ApiModelProperty(value = "销量")
    private Long fsellNum;

    /** 初始库存 */
    @ApiModelProperty(value = "初始库存")
    private Long fstockNum;

    /** 剩余库存 */
    @ApiModelProperty(value = "剩余库存")
    private Long fstockRemianNum;

    /** 冻结库存 */
    @ApiModelProperty(value = "冻结库存")
    private Long fstockFrozenNum;

    /** 批次价格类型 1.含邮含税 2.含邮不含税 3.不含邮含税4.不含邮不含税 */
    @ApiModelProperty(value = "批次价格类型 1.含邮含税 2.含邮不含税 3.不含邮含税4.不含邮不含税")
    private Integer fbatchPriceType;

    /** 单位重量(KG/件) */
    @ApiModelProperty(value = "单位重量(KG/件)")
    private Long ffreight;

    /** 批次状态 1下架 2上架 3 已售完 4已过期 */
    @ApiModelProperty(value = "批次状态 1下架 2上架 3 已售完 4已过期")
    private Integer fbatchStatus;

    /** 库存有效期起始日期 */
    @ApiModelProperty(value = "库存有效期起始日期")
    private Date fvalidityStartDate;

    /** 库存有效期结束日期 */
    @ApiModelProperty(value = "库存有效期结束日期")
    private Date fvalidityEndDate;

}