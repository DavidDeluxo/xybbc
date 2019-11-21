package com.xingyun.bbc.mallpc.service.impl;

import com.google.common.collect.Lists;
import com.xingyun.bbc.core.order.api.ShopcarApi;
import com.xingyun.bbc.core.order.api.ShopcarFinishApi;
import com.xingyun.bbc.core.order.po.Shopcar;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.enums.GoodsTradeType;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.utils.FileUtils;
import com.xingyun.bbc.mallpc.common.utils.RandomUtils;
import com.xingyun.bbc.mallpc.model.dto.BaseDto;
import com.xingyun.bbc.mallpc.model.dto.shoppingcart.ShoppingCartDto;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ShoppingCartCheckoutVo;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ShoppingCartGoodsVo;
import com.xingyun.bbc.mallpc.model.vo.shoppingcart.ShoppingCartVo;
import com.xingyun.bbc.mallpc.service.ShoppingCartService;
import org.apache.commons.collections.CollectionUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
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

    @Autowired
    private DozerHolder dozerHolder;

    @Autowired
    private Mapper dozerMapper;

    @Override
    public Result add(ShoppingCartDto shoppingCartDto) {
        return Result.success();
    }

    @Override
    public Result qty(ShoppingCartDto shoppingCartDto) {
        return Result.success(new ShoppingCartVo().setQty(8));
    }

    @Override
    public Result editNum(BaseDto baseDto) {
        return Result.success();
    }

    @Override
    public Result delete(ShoppingCartDto shoppingCartDto) {
        return Result.success();
    }

    @Override
    public Result<ShoppingCartVo> show(ShoppingCartDto shoppingCartDto) {
        Result<List<Shopcar>> queryShopcarResult = shopcarApi.queryByCriteria(Criteria.of(Shopcar.class).
                andEqualTo(Shopcar::getFuid, shoppingCartDto.getUserId()).sortDesc(Shopcar::getFcreateTime));
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

    @Override
    public Result<ShoppingCartCheckoutVo> checkout(ShoppingCartDto shoppingCartDto) {
        return Result.success(new ShoppingCartCheckoutVo().setBondedQty(1).setNonBondedQty(1));
    }

    /**
     * @param shopcar
     * @return
     */
    private ShoppingCartGoodsVo getShoppingCartGoodsVo(Shopcar shopcar) {
        ShoppingCartGoodsVo shoppingCartGoodsVo = dozerMapper.map(shopcar, ShoppingCartGoodsVo.class);
        shoppingCartGoodsVo.setFskuThumbImage(FileUtils.getFileUrl("M00/00/3F/wKgCkV2lNquAQ5ZAAAA3Rf7UAvw630.png"));//todo
        shoppingCartGoodsVo.setTradeType(GoodsTradeType.findByCode(shopcar.getFtradeType()).getDesc());
        shoppingCartGoodsVo.setBatchStartNum(shopcar.getFbatchNum() + "件起发");//todo
        shoppingCartGoodsVo.setBatchPackageNum(shopcar.getFbatchPackageNum() + "件装");//todo
        shoppingCartGoodsVo.setSkuPrice(BigDecimal.valueOf(shopcar.getFskuPrice()).multiply(BigDecimal.valueOf(100)).longValue());
        shoppingCartGoodsVo.setSurplusStock(RandomUtils.randomInt(1, 10));//todo
        return shoppingCartGoodsVo;
    }

}
