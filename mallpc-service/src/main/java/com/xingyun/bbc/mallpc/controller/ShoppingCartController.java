package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.shoppingcart.ShoppingCartDto;
import com.xingyun.bbc.mallpc.model.validation.ShoppingCartValidator;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ShoppingCartGoodsVo;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ShoppingCartVo;
import com.xingyun.bbc.mallpc.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 进货单相关接口
 *
 * @author penglu
 * @version 1.0.0
 * @date 2019-11-19
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@RestController
@RequestMapping(value = "shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 加入商品
     *
     * @param shoppingCartDto
     * @return
     */
    @PostMapping("add")
    public Result add(@RequestBody @Validated(ShoppingCartValidator.Add.class) ShoppingCartDto shoppingCartDto) {
        return shoppingCartService.add(shoppingCartDto);
    }

    /**
     * 角标数量
     *
     * @param shoppingCartDto
     * @return
     */
    @PostMapping("qty")
    public Result qty(@RequestBody @Validated(ShoppingCartValidator.Qty.class) ShoppingCartDto shoppingCartDto) {
        return shoppingCartService.qty(shoppingCartDto);
    }

    /**
     * 编辑规格数量
     *
     * @param shoppingCartDto
     * @return
     */
    @PostMapping("editNum")
    public Result editNum(@RequestBody @Validated(ShoppingCartValidator.EditNum.class) ShoppingCartDto shoppingCartDto) {
        return shoppingCartService.editNum(shoppingCartDto);
    }

    /**
     * 删除进货单商品
     *
     * @param shoppingCartDto
     * @return
     */
    @PostMapping("delete")
    public Result delete(@RequestBody @Validated(ShoppingCartValidator.Delete.class) ShoppingCartDto shoppingCartDto) {
        return shoppingCartService.delete(shoppingCartDto);
    }

    /**
     * 展示购物车商品列表
     *
     * @param shoppingCartDto
     * @return
     */
    @PostMapping("show")
    public Result<ShoppingCartVo> show(@RequestBody @Validated(ShoppingCartValidator.Show.class) ShoppingCartDto shoppingCartDto) {
        return shoppingCartService.show(shoppingCartDto);
    }

    /**
     * 结算商品
     *
     * @param shoppingCartDto
     * @return
     */
    @PostMapping("checkout")
    public Result checkout(@RequestBody @Validated(ShoppingCartValidator.Checkout.class) ShoppingCartDto shoppingCartDto) {
        return shoppingCartService.checkout(shoppingCartDto);
    }

    /**
     * 刷新商品
     *
     * @param shoppingCartDto
     * @return
     */
    @PostMapping("refresh")
    public Result<List<ShoppingCartGoodsVo>> refresh(@RequestBody @Validated(ShoppingCartValidator.Refresh.class) ShoppingCartDto shoppingCartDto) {
        return shoppingCartService.refresh(shoppingCartDto);
    }

}
