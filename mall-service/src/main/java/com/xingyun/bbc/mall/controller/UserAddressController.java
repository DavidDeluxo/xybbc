package com.xingyun.bbc.mall.controller;


import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.*;
import com.xingyun.bbc.mall.model.vo.CityRegionVo;
import com.xingyun.bbc.mall.model.vo.PageVo;
import com.xingyun.bbc.mall.model.vo.UserDeliveryVo;
import com.xingyun.bbc.mall.service.UserAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * @author lll
 * @Description: 用户收货地址
 * @createTime: 2019-09-03 11:00
 */
@Validated
@Api("用户收货地址")
@RestController
@RequestMapping("/userAddress")
public class UserAddressController {
    public static final Logger logger = LoggerFactory.getLogger(UserAddressController.class);
    @Autowired
    private UserAddressService userAddressService;

    @ApiOperation(value = "查询用户收货地址列表", httpMethod = "POST")
    @PostMapping("/getUserAddress")
    public Result<PageVo<UserDeliveryVo>> getUserAddress(@RequestBody UserDeliveryDto userDeliveryDto, HttpServletRequest request) {
        userDeliveryDto.setFuid(Long.parseLong(request.getHeader("xyid")));
        return Result.success(userAddressService.getUserAddress(userDeliveryDto));
    }

    @ApiOperation(value = "新增用户收货地址", httpMethod = "POST")
    @PostMapping("/addUserAddress")
    public Result addUserAddress(@RequestBody @Validated UserDeliveryAddDto userDeliveryDto, HttpServletRequest request) {
        userDeliveryDto.setFuid(Long.parseLong(request.getHeader("xyid")));
        return userAddressService.addUserAddress(userDeliveryDto);
    }

    @ApiOperation(value = "编辑用户收货地址", httpMethod = "POST")
    @PostMapping("/modifyUserAddress")
    public Result modifyUserAddress(@RequestBody UserDeliveryUpdateDto userDeliveryDto, HttpServletRequest request) {
        userDeliveryDto.setFuid(Long.parseLong(request.getHeader("xyid")));
        return userAddressService.modifyUserAddress(userDeliveryDto);
    }

    @ApiOperation(value = "删除用户收货地址", httpMethod = "POST")
    @PostMapping("/deleteUserAddress")
    public Result deleteUserAddress(@RequestBody @Validated UserDeliveryDeleteDto userDeliveryDto, HttpServletRequest request) {
        return userAddressService.deleteUserAddress(userDeliveryDto);
    }


    @ApiOperation(value = "收件地址查询区域列表", httpMethod = "POST")
    @PostMapping("/getCityRegionlis")
    public Result<List<CityRegionVo>> getCityRegionLis(@RequestBody CityRegionDto cityRegionDto) {
        return userAddressService.getCityRegionLis(cityRegionDto);
    }



}
