package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.utils.JwtParser;
import com.xingyun.bbc.mallpc.model.dto.address.*;
import com.xingyun.bbc.mallpc.model.vo.PageVo;
import com.xingyun.bbc.mallpc.model.vo.TokenInfoVo;
import com.xingyun.bbc.mallpc.model.vo.address.CityRegionVo;
import com.xingyun.bbc.mallpc.model.vo.address.UserAddressDetailsVo;
import com.xingyun.bbc.mallpc.model.vo.address.UserAddressListVo;
import com.xingyun.bbc.mallpc.service.UserAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-18
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@RestController
@Api("收件地址列表")
@RequestMapping("/userAddress")
public class UserAddressController {

    private final UserAddressService userAddressService;

    private final JwtParser jwtParser;

    @Autowired
    public UserAddressController(UserAddressService userAddressService, JwtParser jwtParser) {
        this.userAddressService = userAddressService;
        this.jwtParser = jwtParser;
    }

    @ApiOperation("收货地址列表查询")
    @PostMapping("/query")
    public Result<PageVo<UserAddressListVo>> query(@RequestBody UserAddressListDto userAddressListDto) {
        return userAddressService.query(userAddressListDto);
    }

    @ApiOperation("新增或编辑收货地址")
    @PostMapping("/saveOrUpdate")
    public Result saveOrUpdate(@Validated @RequestBody UserAddressDto userAddressDto) {
        return userAddressService.saveOrUpdate(userAddressDto);
    }

    @ApiOperation("收货地址列表详情")
    @PostMapping("/view")
    public Result<UserAddressDetailsVo> view(@Validated @RequestBody UserAddressDetailsDto userAddressDetailsDto) {
        return userAddressService.view(userAddressDetailsDto);
    }

    @ApiOperation("删除收货地址")
    @PostMapping("/del")
    public Result del(@Validated @RequestBody UserAddressDetailsDto userAddressDetailsDto) {
        return userAddressService.del(userAddressDetailsDto);
    }

    @ApiOperation(value = "收件地址查询区域列表", httpMethod = "POST")
    @PostMapping("/queryCityRegion")
    public Result<List<CityRegionVo>> getCityRegionLis(@Validated @RequestBody CityRegionDto cityRegionDto) {
        return userAddressService.getCityRegionLis(cityRegionDto);
    }

    @ApiOperation("默认收货地址")
    @GetMapping("/default")
    public Result<UserAddressDetailsVo> defaultAddress(HttpServletRequest request) {
        TokenInfoVo infoVo = jwtParser.getTokenInfo(request);
        Assert.notNull(infoVo.getFuid(), "用户不能为空");
        return Result.success(userAddressService.defaultAddress(infoVo.getFuid()));
    }

    @ApiOperation(value = "查询收件地址 确认订单页", httpMethod = "POST")
    @PostMapping("/queryAddress")
    public Result<List<UserAddressListVo>> queryAddress(@RequestBody UserDeliveryListDto userDeliveryListDto){
        return userAddressService.queryAddress(userDeliveryListDto);
    }
}
