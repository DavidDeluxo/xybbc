package com.xingyun.bbc.mallpc.model.vo.shoppingcart;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-11-19
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
@Accessors(chain = true)
public class ShoppingCartGoodsVo implements Serializable {

    private static final long serialVersionUID = 8446980144270805694L;

    /**
     * 进货单商品ID
     */
    private Long fshopcarId;

    /**
     * 商品Id
     */
    private Long fgoodsId;

    /**
     * sku编号
     */
    private String fskuId;

    /**
     * sku缩略图
     */
    private String fskuThumbImage;

    /**
     * sku名称
     */
    private String fskuName;

    /**
     * 贸易类型
     */
    private String tradeType;

    /**
     * 批次起发数
     */
    private Long batchStartNum;

    /**
     * 批次包装规格值
     */
    private String batchPackageNum;

    /**
     * 保质有效期起始日期，yyyy.MM
     */
    private String fqualityStartDate;

    /**
     * 保质有效期结束日期，yyyy.MM
     */
    private String fqualityEndDate;

    /**
     * 保质有效期直接拼好 20年02月~20年05月
     */
    private String fqualityDateStr;

    /**
     * sku销售价
     */
    private BigDecimal fskuPrice;

    /**
     * 购买数量
     */
    private Integer fskuNum;

    /**
     * 剩余库存
     */
    private Long fstockRemianNum;

    /**
     * 是否保税商品
     */
    private boolean bondedGoods;

}
