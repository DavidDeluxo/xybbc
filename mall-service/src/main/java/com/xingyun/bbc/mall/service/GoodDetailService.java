package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.GoodsDetailMallDto;
import com.xingyun.bbc.mall.model.dto.ReceiveCouponDto;
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

    //获取价格
    Result<GoodsPriceVo> getGoodPrice(GoodsDetailMallDto goodsDetailMallDto);

    //获取库存
    Result<GoodStockSellVo> getGoodStock(GoodsDetailMallDto goodsDetailMallDto);

    //获取销量
    Result<GoodStockSellVo> getGoodSell(GoodsDetailMallDto goodsDetailMallDto);

    //查询是否已经加入常购清单
    Result<Integer> getIsRegular(Long fgoodsId, Long fuid);

    //商品详情查询可领取优惠券--未点击
    Result<List<CouponVo>> getSkuUserCouponLight(Long fskuId, Long fuid);

    //商品详情查询可领取优惠券--点击
    Result<GoodsDetailCouponVo> getSkuUserCoupon(Long fskuId, Long fuid);

    //查询优惠券使用说明
    Result<String> getCouponInstructions (Long fcouponId);

    //商品详情领取优惠券
    Result addReceiveCoupon(Long fcouponId, Long fuid);

    //领取优惠券--通用
    Result receiveCoupon(ReceiveCouponDto receiveCouponDto);

}
