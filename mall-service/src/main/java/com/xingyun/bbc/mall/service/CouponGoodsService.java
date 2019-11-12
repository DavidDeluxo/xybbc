package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.CouponGoodsDto;
import com.xingyun.bbc.mall.model.vo.SearchItemListVo;
import com.xingyun.bbc.mall.model.vo.SearchItemVo;

/**
 * @author hekaijin
 * @date 2019/11/11 11:36
 * @Description
 */
public interface CouponGoodsService {

    /**
     * 根据优惠券条件查询商品
     * @param dto
     * @return
     */
    Result<SearchItemListVo<SearchItemVo>> queryGoodsList(CouponGoodsDto dto);

}
