package com.xingyun.bbc.mallpc.service;


import com.xingyun.bbc.core.activity.model.dto.CouponQueryDto;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.vo.ReceiveCenterCouponVo;
import com.xingyun.bbc.mallpc.model.dto.coupon.MyCouponDto;
import com.xingyun.bbc.mallpc.model.vo.coupon.MyCouponVo;

import java.util.List;


public interface MyCouponService {

    /**
     * @author lll
     * @version V1.0
     * @Description: 获取我的优惠券列表
     * @Param: myCouponDto
     * @return: MyCouponVo                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    Result<MyCouponVo> getMyCouponVo(MyCouponDto myCouponDto);


    /**
     * @author lll
     * @version V1.0
     * @Description: 领取优惠券
     * @Param: receiveCouponDto
     * @return: Result                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    Result addReceiveCoupon(Long fcouponId, Long fuid);

    /**
     * @author lll
     * @version V1.0
     * @Description: 查询可领优惠券
     * @Param: receiveCouponDto
     * @return: List<CouponCenterVo>                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    Result<List<ReceiveCenterCouponVo>> getCoupon(CouponQueryDto couponQueryDto);

}
