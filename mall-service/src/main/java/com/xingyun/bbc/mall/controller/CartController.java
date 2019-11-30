package com.xingyun.bbc.mall.controller;

import com.alibaba.fastjson.JSON;
import com.xingyun.bbc.core.order.api.ShopcarApi;
import com.xingyun.bbc.core.order.po.Shopcar;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.UserUtil;
import com.xingyun.bbc.order.api.CartApi;
import com.xingyun.bbc.order.model.dto.cart.*;
import com.xingyun.bbc.order.model.vo.PageVo;
import com.xingyun.bbc.order.model.vo.cart.CartsVo;
import io.seata.common.util.CollectionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author hekaijin
 * @Description:
 * @createTime: 2019-09-04 11:49
 */
@Api("购物车")
@RestController
@RequestMapping("/cart")
@Validated
@Slf4j
public class CartController {

    @Autowired
    private CartApi cartApi;
    @Autowired
    private ShopcarApi shopcarApi;

    @ApiOperation("添加购物车")
    @PostMapping("/addCart")
    public Result<Boolean> addCart(@RequestBody @Valid AddCartDto addCartDto, HttpServletRequest request) {
        Long fuid = UserUtil.uid(request);
        log.info("|接口:|添加购物车|请求参数:{}|", JSON.toJSONString(addCartDto));
        return cartApi.addCart(addCartDto.setFuid(fuid));
    }


    @ApiOperation("查询购物车")
    @PostMapping("/queryCart")
    public Result<PageVo<CartsVo>> queryCart(@RequestBody @Valid QueryCartDto queryCartDto, HttpServletRequest request) {
        Long fuid = UserUtil.uid(request);
        log.info("|接口:|查询购物车|请求参数:{}|", JSON.toJSONString(queryCartDto));
        try {
            Result<List<Shopcar>> idsRes = shopcarApi.queryByCriteria(Criteria.of(Shopcar.class)
                    .andEqualTo(Shopcar::getFuid, fuid).fields(Shopcar::getFshopcarId)
                    .sortDesc(Shopcar::getFcreateTime).page(queryCartDto.getPageNum(),queryCartDto.getPageSize()));
            if (idsRes.isSuccess()&& CollectionUtils.isNotEmpty(idsRes.getData())){
                List<Long> ids = idsRes.getData().stream().map(Shopcar::getFshopcarId).collect(Collectors.toList());
                cartApi.refresh(new CartRefreshDto().setShopCarIds(ids).setUserId(fuid));
            }
        }catch (Exception e){
            log.warn("购物车刷新数量无效...");
        }

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

    @ApiOperation("刷新商品")
    @PostMapping("refresh")
    public Result<List<CartsVo>> refresh(@RequestBody CartClearDto refreshDto,HttpServletRequest request) {
        Long fuid = UserUtil.uid(request);
        return cartApi.refresh(new CartRefreshDto().setShopCarIds(refreshDto.getFshopcarIds()).setUserId(fuid));
    }
}
