package com.xingyun.bbc.mall.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.common.utils.RequestHolder;
import com.xingyun.bbc.mall.model.dto.MyCouponDto;
import com.xingyun.bbc.mall.model.vo.MyCouponVo;
import com.xingyun.bbc.mall.service.MyCouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api("我的优惠券")
@RestController
@RequestMapping("/myCoupon")
public class MyCouponController {

    @Autowired
    private MyCouponService myCouponService;

    @ApiOperation(value = "获取我的优惠券列表", httpMethod = "GET")
    @GetMapping("/getMyCouponLis")
    public Result<MyCouponVo> getMyCouponLis(@ModelAttribute MyCouponDto myCouponDto, HttpServletRequest request) {
        Long xyid = RequestHolder.getUserId();
        myCouponDto.setFuid(xyid);
        return myCouponService.getMyCouponVo(myCouponDto);
    }
}
