package com.xingyun.bbc.mall.controller;



import com.xingyun.bbc.core.activity.model.dto.CouponQueryDto;
import com.xingyun.bbc.core.utils.Result;

import com.xingyun.bbc.mall.model.dto.ReceiveCouponDto;

import com.xingyun.bbc.mall.model.vo.ReceiveCenterCoupon;
import com.xingyun.bbc.mall.service.GoodDetailService;
import com.xingyun.bbc.mall.service.ReceiveCenterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.List;


@Api("领券中心")
@RestController
@RequestMapping("/receiveCenter")
public class ReceiveCenterController {

    public static final Logger logger = LoggerFactory.getLogger(ReceiveCenterController.class);

    @Autowired
    GoodDetailService goodDetailService;

    @Autowired
    ReceiveCenterService receiveCenterService;

    /**
     * @author lll
     * @version V1.0
     * @Description: 领券中心领取优惠券
     * @Param: receiveCouponDto
     * @return: Boolean                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    @ApiOperation(value = "领券中心领取优惠券", httpMethod = "Post")
    @PostMapping("/receiveCoupon")
    public Result<Boolean> receiveCoupon(@RequestBody ReceiveCouponDto receiveCouponDto, HttpServletRequest request){
        Long xyid = Long.parseLong(request.getHeader("xyid"));
        logger.info("领券中心领取优惠券 fcouponId {} fuid {}", receiveCouponDto.getFcouponId(), xyid);
        return goodDetailService.addReceiveCoupon(receiveCouponDto.getFcouponId(), xyid);
    }

    /**
     * @author lll
     * @version V1.0
     * @Description: 券码兑换优惠券
     * @Param: receiveCouponDto
     * @return: Boolean                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    @ApiOperation(value = "券码兑换优惠券", httpMethod = "Post")
    @PostMapping("/receiveCodeCoupon")
    public Result<Boolean> receiveCodeCoupon(@RequestBody ReceiveCouponDto receiveCouponDto, HttpServletRequest request){
        Long xyid = Long.parseLong(request.getHeader("xyid"));
        logger.info("券码兑换优惠券 fcouponCode {} fuid {}", receiveCouponDto.getFcouponCode(), xyid);
        return receiveCenterService.receiveCodeCoupon(receiveCouponDto.getFcouponCode(), xyid);
    }

    /**
     * @author lll
     * @version V1.0
     * @Description: 查询领券中心优惠券
     * @Param: ReceiveCenterCoupon
     * @return: List<CouponCenterVo>                                                                                                                                                                                                                                                                 <                                                                                                                                                                                                                                                               GoodsCategoryVo>>
     * @date 2019/11/12 13:49
     */
    @ApiOperation(value = "查询领券中心优惠券", httpMethod = "Post")
    @PostMapping("/getCoupon")
    public Result<List<ReceiveCenterCoupon>> getCoupon(HttpServletRequest request, @RequestBody CouponQueryDto couponQueryDto){
        Long xyid = Long.parseLong(request.getHeader("xyid"));
        couponQueryDto.setUserId(xyid);
        logger.info("查询领券中心优惠券 fuid {}",  xyid);
        return receiveCenterService.getCoupon(couponQueryDto);
    }
}
