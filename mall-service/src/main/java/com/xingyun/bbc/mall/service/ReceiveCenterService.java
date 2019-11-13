package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;


public interface ReceiveCenterService {

    /**
     * @author lll
     * @version V1.0
     * @Description: 券码兑换优惠券
     * @Param: receiveCouponDto
     * @return: Boolean                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    Result<Boolean> receiveCodeCoupon(String fcouponCode, Long fuid);






}
