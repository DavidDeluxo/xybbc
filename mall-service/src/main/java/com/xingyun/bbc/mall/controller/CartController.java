package com.xingyun.bbc.mall.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.UserUtil;
import com.xingyun.bbc.order.api.CartApi;
import com.xingyun.bbc.order.model.dto.cart.AddCartDto;
import com.xingyun.bbc.order.model.dto.cart.CartClearDto;
import com.xingyun.bbc.order.model.dto.cart.CartNumDto;
import com.xingyun.bbc.order.model.dto.cart.QueryCartDto;
import com.xingyun.bbc.order.model.vo.PageVo;
import com.xingyun.bbc.order.model.vo.cart.CartsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @author hekaijin
 * @Description:
 * @createTime: 2019-09-04 11:49
 */
@Api("购物车")
@RestController
@RequestMapping("/cart")
@Validated
public class CartController {

    @Autowired
    private CartApi cartApi;

    @ApiOperation("添加购物车")
    @PostMapping("/addCart")
    public Result<Boolean> addCart(@RequestBody @Valid AddCartDto addCartDto, HttpServletRequest request) {
        Long fuid = UserUtil.uid(request);
        return cartApi.addCart(addCartDto.setFuid(fuid));
    }


    @ApiOperation("查询购物车")
    @PostMapping("/queryCart")
    public Result<PageVo<CartsVo>> queryCart(@RequestBody @Valid QueryCartDto queryCartDto, HttpServletRequest request) {
        Long fuid = UserUtil.uid(request);
        return cartApi.queryCart(queryCartDto.setFuid(fuid));
    }

    @ApiOperation("查询购物车数量")
    @PostMapping("/count")
    public Result<Integer> countCart(HttpServletRequest request) {
        return cartApi.countCart(UserUtil.uid(request));
    }


    @ApiOperation("删除/清空购物车")
    @PostMapping("/clear")
    public Result<Boolean> clear(@RequestBody @Valid CartClearDto clearDto, HttpServletRequest request) {
        Long fuid = UserUtil.uid(request);
        return cartApi.clear(clearDto.setFuid(fuid));
    }

    @ApiOperation("更新购物车数量")
    @PostMapping("/modifyNum")
    public Result<Boolean> modifyNum(@RequestBody @Valid CartNumDto cartNumDto, HttpServletRequest request) {
        Long fuid = UserUtil.uid(request);
        return cartApi.modifyNum(cartNumDto.setFuid(fuid));
    }


}
