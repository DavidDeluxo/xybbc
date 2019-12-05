package com.xingyun.bbc.mallpc.service.impl;

import com.google.common.collect.Lists;
import com.xingyun.bbc.core.sku.enums.GoodsTradeType;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.FileUtils;
import com.xingyun.bbc.mallpc.common.utils.RequestHolder;
import com.xingyun.bbc.mallpc.model.dto.shoppingcart.ShoppingCartDto;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ShoppingCartGoodsVo;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ShoppingCartVo;
import com.xingyun.bbc.mallpc.service.ShoppingCartService;
import com.xingyun.bbc.order.api.CartApi;
import com.xingyun.bbc.order.api.OrderSettleSplitApi;
import com.xingyun.bbc.order.model.dto.cart.*;
import com.xingyun.bbc.order.model.dto.order.OrderSettleDto;
import com.xingyun.bbc.order.model.vo.PageVo;
import com.xingyun.bbc.order.model.vo.cart.CartsVo;
import com.xingyun.bbc.order.model.vo.order.OrderSettleVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-11-21
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    /**
     * 进货单商品数量上限
     */
    private static final Integer SHOPPINGCART_GOODS_QTY_LIMIT = 120;

    @Resource
    private CartApi cartApi;

    @Resource
    private OrderSettleSplitApi orderSettleSplitApi;

    @Autowired
    private Mapper dozerMapper;

    /**
     * 加入商品
     *
     * @param shoppingCartDto
     * @return
     */
    @Override
    public Result add(ShoppingCartDto shoppingCartDto) {
        return cartApi.addCart(new AddCartDto().
                setFuid(RequestHolder.getUserId()).
                setFskuId(shoppingCartDto.getFskuId()).
                setFbatchId(shoppingCartDto.getFbatchId()).
                setFbatchPackageId(shoppingCartDto.getFbatchPackageId()).
                setFbuyNum(shoppingCartDto.getFbuyNum()));
    }

    /**
     * 角标数量
     *
     * @param shoppingCartDto
     * @return
     */
    @Override
    public Result qty(ShoppingCartDto shoppingCartDto) {
        Result<Integer> countShopcarResult = cartApi.countCart(RequestHolder.getUserId());
        Integer shopcarGoodsQty = countShopcarResult.getData();
        ShoppingCartVo shoppingCartVo = new ShoppingCartVo();
        if (Objects.isNull(shopcarGoodsQty)) {
            return Result.success(shoppingCartVo.setQty("0"));
        }
        if (shopcarGoodsQty > 99) {
            return Result.success(shoppingCartVo.setQty("99+"));
        } else {
            return Result.success(shoppingCartVo.setQty(shopcarGoodsQty.toString()));
        }
    }

    /**
     * 编辑规格数量
     *
     * @param shoppingCartDto
     * @return
     */
    @Override
    public Result editNum(ShoppingCartDto shoppingCartDto) {
        return cartApi.modifyNum(new CartNumDto().
                setFuid(RequestHolder.getUserId()).
                setFshopcarId(shoppingCartDto.getId()).
                setFbuyNum(shoppingCartDto.getFbuyNum()));
    }

    /**
     * 删除进货单商品
     *
     * @param shoppingCartDto
     * @return
     */
    @Override
    public Result delete(ShoppingCartDto shoppingCartDto) {
        return cartApi.clear(new CartClearDto().
                setIsClearAll(BooleanUtils.toBoolean(shoppingCartDto.getClearInvalidGoods()) ? 1 : 0).
                setFshopcarIds(shoppingCartDto.getIds()).
                setFuid(RequestHolder.getUserId()));
    }

    /**
     * 展示购物车商品列表
     *
     * @param shoppingCartDto
     * @return
     */
    @Override
    public Result<ShoppingCartVo> show(ShoppingCartDto shoppingCartDto) {
        ShoppingCartVo shoppingCartVo = new ShoppingCartVo();
        List<ShoppingCartGoodsVo> validGoods = Lists.newArrayList();
        shoppingCartVo.setValidGoods(validGoods);
        List<ShoppingCartGoodsVo> invalidGoods = Lists.newArrayList();
        shoppingCartVo.setInvalidGoods(invalidGoods);
        QueryCartDto queryCartDto = new QueryCartDto().setFuid(RequestHolder.getUserId());
        queryCartDto.setPageNum(1);
        queryCartDto.setPageSize(SHOPPINGCART_GOODS_QTY_LIMIT);
        Result<PageVo<CartsVo>> result = cartApi.queryCart(queryCartDto);
        Ensure.that(result).isSuccess(new MallPcExceptionCode(result.getCode(), result.getMsg()));
        List<CartsVo> cartsVos = result.getData().getList();
        if (CollectionUtils.isEmpty(cartsVos)) {
            return Result.success(shoppingCartVo);
        }
        cartsVos.forEach(cartsVo -> {
            ShoppingCartGoodsVo shoppingCartGoodsVo = dozerMapper.map(cartsVo, ShoppingCartGoodsVo.class);
            shoppingCartGoodsVo.setFstockRemianNum(cartsVo.getFstockRemianNum() / cartsVo.getFbatchPackageNum());
            shoppingCartGoodsVo.setFskuThumbImage(FileUtils.getFileUrl(shoppingCartGoodsVo.getFskuThumbImage()));
            shoppingCartGoodsVo.setBatchStartNum(cartsVo.getFbatchStartNum());
            shoppingCartGoodsVo.setBatchPackageNum(cartsVo.getFbatchPackageNum() + "件装");
            GoodsTradeType goodsTradeType = GoodsTradeType.findByCode(cartsVo.getFtradeType());
            shoppingCartGoodsVo.setTradeType(goodsTradeType.getDesc());
            shoppingCartGoodsVo.setBondedGoods(goodsTradeType == GoodsTradeType.CROSS_BORDER_BONDED);
            if (Objects.equals(cartsVo.getIsEffective(), 1)) {
                validGoods.add(shoppingCartGoodsVo);
            } else {
                invalidGoods.add(shoppingCartGoodsVo);
            }
        });
        return Result.success(shoppingCartVo);
    }

    /**
     * 结算商品
     *
     * @param shoppingCartDto
     * @return
     */
    @Override
    public Result<OrderSettleVo> checkout(ShoppingCartDto shoppingCartDto) {
        OrderSettleDto orderSettleDto = new OrderSettleDto();
        orderSettleDto.setFuid(RequestHolder.getUserId());
        orderSettleDto.setFshopcarIds(StringUtils.join(shoppingCartDto.getIds(), ","));
        return orderSettleSplitApi.launchSettle(orderSettleDto);
    }

    /**
     * 刷新商品
     *
     * @param shoppingCartDto
     * @return
     */
    @Override
    public Result<List<ShoppingCartGoodsVo>> refresh(ShoppingCartDto shoppingCartDto) {
        Result<List<CartsVo>> result = cartApi.refresh(new CartRefreshDto().setShopCarIds(shoppingCartDto.getIds()).setUserId(RequestHolder.getUserId()));
        Ensure.that(result).isSuccess(new MallPcExceptionCode(result.getCode(), result.getMsg()));
        List<CartsVo> cartsVos = result.getData();
        if (CollectionUtils.isEmpty(cartsVos)) {
            return Result.success(Lists.newArrayList());
        }
        List<ShoppingCartGoodsVo> shoppingCartGoodsVos = cartsVos.stream().map(cartsVo -> {
            ShoppingCartGoodsVo shoppingCartGoodsVo = dozerMapper.map(cartsVo, ShoppingCartGoodsVo.class);
            shoppingCartGoodsVo.setFskuThumbImage(FileUtils.getFileUrl(cartsVo.getFskuThumbImage()));
            shoppingCartGoodsVo.setBatchPackageNum(cartsVo.getFbatchPackageNum() + "件装");
            GoodsTradeType goodsTradeType = GoodsTradeType.findByCode(cartsVo.getFtradeType());
            shoppingCartGoodsVo.setTradeType(goodsTradeType.getDesc());
            return shoppingCartGoodsVo;
        }).collect(Collectors.toList());
        return Result.success(shoppingCartGoodsVos);
    }

}
