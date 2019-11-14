package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.activity.model.dto.CouponQueryDto;
import com.xingyun.bbc.core.utils.Result;

import com.xingyun.bbc.mall.model.vo.ReceiveCenterCoupon;



import java.util.List;


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


    /**
     * @author lll
     * @version V1.0
     * @Description: 查询领券中心优惠券
     * @Param: receiveCouponDto
     * @return: List<CouponCenterVo>                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    Result<List<ReceiveCenterCoupon>> getCoupon(CouponQueryDto couponQueryDto);


}
