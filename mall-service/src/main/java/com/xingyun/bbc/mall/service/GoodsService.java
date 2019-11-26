package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.RefreshCouponDto;
import com.xingyun.bbc.mall.model.dto.SearchItemDto;
import com.xingyun.bbc.mall.model.vo.*;

import java.util.List;
import java.util.Map;

public interface GoodsService {

    Result<SearchItemListVo<SearchItemVo>> searchSkuList(SearchItemDto searchItemDto);

    Result<SearchFilterVo> searchSkuFilter(SearchItemDto searchItemDto);

    Result<BrandPageVo> searchSkuBrandPage(Integer fbrandId);

    Result<List<String>> queryHotSearch();

    Result<Boolean> updateCouponList();

    /**
     * 更新ES的优惠券信息 (更新优惠券id列表)
     * @param coupon
     */
    void updateEsSkuWithCouponInfo(Coupon coupon);

    /**
     * 更新ES的优惠券信息 (更新Alias)
     * @param coupon
     */
    void updateCouponInfoToEsByAlias(Coupon coupon) throws Exception;

    void deleteCouponInfoFromEsByAlias(Coupon coupon);

    void deleteCouponInfoFromEsSku(Coupon coupon);

    void updateEsSkuWithSkuUpdate(Map<String, Object> skuSourceMap);

    void updateCouponIdForAllSku(RefreshCouponDto refreshCouponDto);

    void testEsAlias();

}
