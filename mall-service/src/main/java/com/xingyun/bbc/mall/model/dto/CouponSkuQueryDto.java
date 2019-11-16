package com.xingyun.bbc.mall.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author ming.yiFei
 * @ClassName: CouponSkuQueryDto
 * @Description: 优惠券关联商品 - 请求对象
 * @date 2019年11月07日 19:43:46
 */
@Data
public class CouponSkuQueryDto {

    /**
     * 分类id集合
     */
    @ApiModelProperty(value = "分类id集合")
    private Map<String, List<Long>> categoryIds;

    /**
     * 品牌id集合
     */
    @ApiModelProperty(value = "品牌id集合")
    private List<Long> brandIds;

    /**
     * 标签id集合
     */
    @ApiModelProperty(value = "标签id集合")
    private List<Long> labelIds;

    /**
     * 贸易类型id
     */
    @ApiModelProperty(value = "贸易类型id")
    private List<Long> tradeIds;

    /**
     * sku 编码
     */
    @ApiModelProperty(value = "sku 编码")
    private String skuCode;
}
