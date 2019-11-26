package com.xingyun.bbc.mallpc.controller;



import com.xingyun.bbc.core.activity.model.dto.CouponQueryDto;
import com.xingyun.bbc.core.utils.Result;


import com.xingyun.bbc.mallpc.common.utils.RequestHolder;
import com.xingyun.bbc.mallpc.model.dto.coupon.MyCouponDto;
import com.xingyun.bbc.mallpc.model.dto.coupon.ReceiveCouponDto;
import com.xingyun.bbc.mallpc.model.vo.coupon.MyCouponVo;
import com.xingyun.bbc.mallpc.model.vo.coupon.ReceiveCenterCouponVo;
import com.xingyun.bbc.mallpc.service.MyCouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@Api("我的优惠券")
@RestController
@RequestMapping("/myCoupon")
public class MyCouponController {

    public static final Logger logger = LoggerFactory.getLogger(MyCouponController.class);

    @Autowired
    private MyCouponService myCouponService;

    /**
     * @author lll
     * @version V1.0
     * @Description: 获取我的优惠券列表
     * @Param: myCouponDto
     * @return: MyCouponVo                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    @ApiOperation(value = "获取我的优惠券列表", httpMethod = "GET")
    @GetMapping("/getMyCouponList")
    public Result<MyCouponVo> getMyCouponList(@ModelAttribute MyCouponDto myCouponDto, HttpServletRequest request) {
        myCouponDto.setFuid(RequestHolder.getUserId());
        return myCouponService.getMyCouponVo(myCouponDto);
    }


    /**
     * @author lll
     * @version V1.0
     * @Description: 领取优惠券
     * @Param: receiveCouponDto
     * @return: Boolean                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    @ApiOperation(value = "领取优惠券", httpMethod = "POST")
    @PostMapping("/receiveCoupon")
    public Result<Boolean> receiveCoupon(@RequestBody ReceiveCouponDto receiveCouponDto, HttpServletRequest request){
        Long xyid = RequestHolder.getUserId();
        logger.info("领取优惠券 fcouponId {} fuid {}", receiveCouponDto.getFcouponId(), xyid);
        return myCouponService.addReceiveCoupon(receiveCouponDto.getFcouponId(), xyid);
    }

    /**
     * @author lll
     * @version V1.0
     * @Description: 查询可领优惠券
     * @Param: ReceiveCenterCouponVo
     * @return: List<CouponCenterVo>                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    @ApiOperation(value = "查询可领优惠券", httpMethod = "POST")
    @PostMapping("/getCoupon")
    public Result<List<ReceiveCenterCouponVo>> getCoupon(HttpServletRequest request, @RequestBody CouponQueryDto couponQueryDto){
        couponQueryDto.setUserId(RequestHolder.getUserId());
        logger.info("查询可领优惠券 fuid {}",  RequestHolder.getUserId());
        return myCouponService.getCoupon(couponQueryDto);
    }
}
