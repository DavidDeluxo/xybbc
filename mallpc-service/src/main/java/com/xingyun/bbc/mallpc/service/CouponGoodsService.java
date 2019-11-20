package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.search.SearchItemDto;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemListVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemVo;

/**
 * @date 2019/11/20 11:36
 * @Description
 */
public interface CouponGoodsService {

    /**
     * 根据优惠券条件查询商品
     * @param dto
     * @return
     */
    Result<SearchItemListVo<SearchItemVo>> queryGoodsList(SearchItemDto dto);


    /**
     * 根据优惠券条件查询商品-sql
     * @param dto
     * @return
     */
    Result<SearchItemListVo<SearchItemVo>> queryGoodsListRealTime(SearchItemDto dto);
}