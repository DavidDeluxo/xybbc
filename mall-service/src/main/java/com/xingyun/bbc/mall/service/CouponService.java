package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.activity.model.dto.CouponQueryDto;
import com.xingyun.bbc.core.market.po.Coupon;

import java.util.List;

public interface CouponService {

    public List<Coupon> queryBySkuId(CouponQueryDto couponQueryDto);
}
