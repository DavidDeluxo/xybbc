package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.GoodsDetailDto;
import com.xingyun.bbc.mall.model.vo.*;

import java.util.List;
import java.util.Map;

public interface GoodDetailService {

    //获取商品基本信息
    Result<GoodsVo> getGoodDetailBasic(Long fgoodsId, Long fskuId);

    //获取商品主图
    Result<List<String>> getGoodDetailPic(Long fgoodsId, Long fskuId);

    //获取商品属性
    Result<Map<String, List<GoodsAttributeVo>>> getGoodsAttribute(Long fgoodsId);

    //获取各种规格
    Result<GoodspecificationVo> getGoodsSpecifi(Long fgoodsId);

//    //获取sku批次有效期
//    Result<List<GoodsSkuBatchVo>> getSkuBatchSpecifi(Long fskuId);
//
//    //获取sku批次包装规格
//    Result<List<GoodsSkuBatchPackageVo>> getSkuBatchPackageSpecifi(Long fskuBatchId);

    //获取价格
    Result<GoodsPriceVo> getGoodPrice(GoodsDetailDto goodsDetailDto);

    //获取库存
    Result<GoodStockSellVo> getGoodStock(GoodsDetailDto goodsDetailDto);

    //获取销量
    Result<GoodStockSellVo> getGoodSell(GoodsDetailDto goodsDetailDto);

    //查询是否已经加入常购清单
    Result<Integer> getIsRegular(Long fgoodsId, Long fuid);

    //商品详情领取优惠券
    Result<Boolean> addReceiveCoupon(Long fcouponId, Long fuid);






}
