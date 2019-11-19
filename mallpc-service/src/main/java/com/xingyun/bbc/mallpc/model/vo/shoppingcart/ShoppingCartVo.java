package com.xingyun.bbc.mallpc.model.vo.shoppingcart;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-11-19
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
@Accessors(chain = true)
public class ShoppingCartVo implements Serializable {

    private static final long serialVersionUID = 2053580874130747444L;

    /**
     * 有效商品列表
     */
    private List<ShoppingCartGoodsVo> validGoods;

    /**
     * 失效商品列表
     */
    private List<ShoppingCartGoodsVo> invalidGoods;


}
