package com.xingyun.bbc.mallpc.model.vo.shoppingcart;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-11-19
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Data
@Accessors(chain = true)
public class ShoppingCartCheckoutVo implements Serializable {

    private static final long serialVersionUID = 2053580874130747444L;

    /**
     * 保税商品数量
     */
    private Integer bondedQty;

    /**
     * 非保税商品数量
     */
    private Integer nonBondedQty;


}
