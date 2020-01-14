package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.detail.GoodsDetailMallDto;
import com.xingyun.bbc.mallpc.model.dto.detail.ReceiveCouponDto;
import com.xingyun.bbc.mallpc.model.vo.detail.*;

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
    Result<String> getCouponInstructions(Long fcouponId);

    //商品详情领取优惠券
    Result addReceiveCoupon(Long fcouponId, Long fuid);

    //领取优惠券--通用
    Result receiveCoupon(ReceiveCouponDto receiveCouponDto);

    /**
     * 获取分类根据skuId
     *
     * @param skuId
     * @return
     */
    Result<Map<String, Long>> getCategoryBySkuId(Long skuId);

    /**
     * @Description 获取sku缩略图
     * @Author ming.yiFei
     * @Date 18:21 2020/1/2
     * @Param [fskuId]
     * @return com.xingyun.bbc.core.utils.Result<jjava.lang.String>
     **/
    Result<String> getGoodSkuPic(Long fskuId);
}
