package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.RefreshCouponDto;
import com.xingyun.bbc.mall.model.dto.SearchItemDto;
import com.xingyun.bbc.mall.model.vo.*;

import java.util.List;
import java.util.Map;

public interface GoodsService {

    /**
     * 查询sku列表
     * @param searchItemDto
     * @return
     */
    Result<SearchItemListVo<SearchItemVo>> searchSkuList(SearchItemDto searchItemDto);

    /**
     * 查询sku过滤信息
     * @param searchItemDto
     * @return
     */
    Result<SearchFilterVo> searchSkuFilter(SearchItemDto searchItemDto);

    /**
     * 查询商品品牌专属页
     * @param fbrandId
     * @return
     */
    Result<BrandPageVo> searchSkuBrandPage(Integer fbrandId);

    /**
     * 查询热门搜索关键词列表
     * @return
     */
    Result<List<String>> queryHotSearch();

    /**
     * 更新ES的优惠券信息 (增量更新Alias)
     * @param coupon
     */
    void updateCouponInfoToEsByAlias(Coupon coupon, boolean isUpdateByMessage) throws Exception;

    /**
     * 更新ES的优惠券信息 (全量更新Alias)
     * @param coupon
     */
    void updateCouponInfoToEsByAliasBatch(RefreshCouponDto refreshCouponDto);

    /**
     * 删除ES的优惠券信息
     * @param coupon
     */
    void deleteCouponInfoFromEsByAlias(Coupon coupon);

    /**
     * 更新ES商品基本信息
     * @param skuSourceMap
     */
   void updateEsSkuWithBaseInfo(Map<String, Object> skuSourceMap, boolean isBaseInfoUpdate);


}
