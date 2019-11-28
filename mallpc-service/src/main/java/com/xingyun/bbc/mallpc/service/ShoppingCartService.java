package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.shoppingcart.ShoppingCartDto;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ShoppingCartGoodsVo;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ShoppingCartVo;

import java.util.List;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-11-21
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public interface ShoppingCartService {

    /**
     * 加入商品
     *
     * @param shoppingCartDto
     * @return
     */
    Result add(ShoppingCartDto shoppingCartDto);

    /**
     * 角标数量
     *
     * @param shoppingCartDto
     * @return
     */
    Result qty(ShoppingCartDto shoppingCartDto);

    /**
     * 编辑规格数量
     *
     * @param shoppingCartDto
     * @return
     */
    Result editNum(ShoppingCartDto shoppingCartDto);

    /**
     * 删除进货单商品
     *
     * @param shoppingCartDto
     * @return
     */
    Result delete(ShoppingCartDto shoppingCartDto);

    /**
     * 展示购物车商品列表
     *
     * @param shoppingCartDto
     * @return
     */
    Result<ShoppingCartVo> show(ShoppingCartDto shoppingCartDto);

    /**
     * 结算商品
     *
     * @param shoppingCartDto
     * @return
     */
    Result checkout(ShoppingCartDto shoppingCartDto);

    /**
     * 刷新商品
     *
     * @param shoppingCartDto
     * @return
     */
    Result<List<ShoppingCartGoodsVo>> refresh(ShoppingCartDto shoppingCartDto);


}
