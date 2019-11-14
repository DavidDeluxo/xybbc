package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.MyCouponDto;
import com.xingyun.bbc.mall.model.vo.MyCouponVo;

public interface MyCouponService {

    Result<MyCouponVo> getMyCouponVo(MyCouponDto myCouponDto);

}
