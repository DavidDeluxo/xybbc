package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.GoodsDetailDto;
import com.xingyun.bbc.mall.model.dto.UserDeliveryDto;
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

    //获取库存和销量
    Result<GoodStockSellVo> getGoodStockSell(GoodsDetailDto goodsDetailDto);





}
