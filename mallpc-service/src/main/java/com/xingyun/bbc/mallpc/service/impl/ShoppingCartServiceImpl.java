package com.xingyun.bbc.mallpc.service.impl;

import com.google.common.collect.Lists;
import com.xingyun.bbc.core.order.api.ShopcarApi;
import com.xingyun.bbc.core.order.api.ShopcarFinishApi;
import com.xingyun.bbc.core.order.po.Shopcar;
import com.xingyun.bbc.core.order.po.ShopcarFinish;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.enums.GoodsTradeType;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.FileUtils;
import com.xingyun.bbc.mallpc.common.utils.RandomUtils;
import com.xingyun.bbc.mallpc.common.utils.RequestHolder;
import com.xingyun.bbc.mallpc.model.dto.shoppingcart.ShoppingCartDto;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ShoppingCartGoodsVo;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ShoppingCartVo;
import com.xingyun.bbc.mallpc.service.ShoppingCartService;
import com.xingyun.bbc.order.api.OrderSettleSplitApi;
import com.xingyun.bbc.order.model.dto.order.OrderSettleDto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
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

    @Resource
    private ShopcarApi shopcarApi;

    @Resource
    private ShopcarFinishApi shopcarFinishApi;

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
        return Result.success();
    }

    /**
     * 角标数量
     *
     * @param shoppingCartDto
     * @return
     */
    @Override
    public Result qty(ShoppingCartDto shoppingCartDto) {
        Result<Integer> countShopcarResult = shopcarApi.countByCriteria(Criteria.of(Shopcar.class).
                andEqualTo(Shopcar::getFuid, RequestHolder.getUserId()));
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
        Result<Shopcar> shopcarResult = shopcarApi.queryById(shoppingCartDto.getId());
        Ensure.that(shopcarResult).isNotNullData(MallPcExceptionCode.SHOPPING_CART_NOT_EXIST);
        Shopcar shopcar = shopcarResult.getData();
        Shopcar updateShopcar = new Shopcar();
        updateShopcar.setFshopcarId(shopcar.getFshopcarId());
        updateShopcar.setFskuNum(shoppingCartDto.getSkuNum());
        Ensure.that(shopcarApi.updateNotNull(updateShopcar)).writeIsSuccess(MallPcExceptionCode.SYSTEM_ERROR);
        return Result.success();
    }

    /**
     * 删除进货单商品
     *
     * @param shoppingCartDto
     * @return
     */
    @Override
    public Result delete(ShoppingCartDto shoppingCartDto) {
        shoppingCartDto.getIds().forEach(shopcarId -> {
            Shopcar shopcar = shopcarApi.queryById(shopcarId).getData();
            if (Objects.isNull(shopcar)) {
                return;
            }
            shopcarFinishApi.create(dozerMapper.map(shopcar, ShopcarFinish.class));
            shopcarApi.deleteById(shopcar.getFshopcarId());

        });
        return Result.success();
    }

    /**
     * 展示购物车商品列表
     *
     * @param shoppingCartDto
     * @return
     */
    @Override
    public Result<ShoppingCartVo> show(ShoppingCartDto shoppingCartDto) {
        Result<List<Shopcar>> queryShopcarResult = shopcarApi.queryByCriteria(Criteria.of(Shopcar.class).
                andEqualTo(Shopcar::getFuid, RequestHolder.getUserId()).sortDesc(Shopcar::getFcreateTime));
        List<Shopcar> shopcars = queryShopcarResult.getData();
        if (CollectionUtils.isEmpty(shopcars)) {
            return Result.success(new ShoppingCartVo().setValidGoods(Lists.newArrayList()).setInvalidGoods(Lists.newArrayList()));
        }
        ShoppingCartVo shoppingCartVo = new ShoppingCartVo();
        List<ShoppingCartGoodsVo> shoppingCartGoodsVos = shopcars.subList(0, shopcars.size() - 1).stream().map(
                shopcar -> getShoppingCartGoodsVo(shopcar)).collect(Collectors.toList());
        shoppingCartVo.setValidGoods(shoppingCartGoodsVos);
        shoppingCartVo.setInvalidGoods((Lists.newArrayList(getShoppingCartGoodsVo(shopcars.get(shopcars.size() - 1)))));
        return Result.success(shoppingCartVo);
    }

    /**
     * 结算商品
     *
     * @param shoppingCartDto
     * @return
     */
    @Override
    public Result checkout(ShoppingCartDto shoppingCartDto) {
        OrderSettleDto orderSettleDto = new OrderSettleDto();
        orderSettleDto.setFuid(RequestHolder.getUserId());
        orderSettleDto.setFshopcarIds(StringUtils.join(shoppingCartDto.getIds(), ","));
        return orderSettleSplitApi.launchSettle(orderSettleDto);
    }

    /**
     * @param shopcar
     * @return
     */
    private ShoppingCartGoodsVo getShoppingCartGoodsVo(Shopcar shopcar) {
        ShoppingCartGoodsVo shoppingCartGoodsVo = dozerMapper.map(shopcar, ShoppingCartGoodsVo.class);
        shoppingCartGoodsVo.setFskuThumbImage(FileUtils.getFileUrl("M00/00/3F/wKgCkV2lNquAQ5ZAAAA3Rf7UAvw630.png"));//todo
        GoodsTradeType goodsTradeType = GoodsTradeType.findByCode(shopcar.getFtradeType());
        shoppingCartGoodsVo.setTradeType(goodsTradeType.getDesc());
        shoppingCartGoodsVo.setBatchStartNum(shopcar.getFbatchNum() + "件起发");//todo
        shoppingCartGoodsVo.setBatchPackageNum(shopcar.getFbatchPackageNum() + "件装");//todo
        shoppingCartGoodsVo.setSkuPrice(BigDecimal.valueOf(shopcar.getFskuPrice()).multiply(BigDecimal.valueOf(100)).longValue());
        shoppingCartGoodsVo.setSurplusStock(RandomUtils.randomInt(1, 10));//todo
        shoppingCartGoodsVo.setBondedGoods(goodsTradeType == GoodsTradeType.CROSS_BORDER_BONDED);
        return shoppingCartGoodsVo;
    }

}
