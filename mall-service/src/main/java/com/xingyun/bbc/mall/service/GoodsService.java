package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.utils.Result;
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

    void updateEsSkuWithCouponInfo(Coupon coupon);

}
