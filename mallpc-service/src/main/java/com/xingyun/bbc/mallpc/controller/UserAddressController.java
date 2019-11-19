package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.address.CityRegionDto;
import com.xingyun.bbc.mallpc.model.dto.address.UserAddressDetailsDto;
import com.xingyun.bbc.mallpc.model.dto.address.UserAddressDto;
import com.xingyun.bbc.mallpc.model.dto.address.UserAddressListDto;
import com.xingyun.bbc.mallpc.model.vo.PageVo;
import com.xingyun.bbc.mallpc.model.vo.address.CityRegionVo;
import com.xingyun.bbc.mallpc.model.vo.address.UserAddressDetailsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation("收货地址列表查询")
    @PostMapping("/query")
    public Result<PageVo<UserAddressListDto>> query(@RequestBody UserAddressListDto userAddressListDto) {
        return null;
    }

    @ApiOperation("新增或编辑收货地址")
    @PostMapping("/saveOrUpdate")
    public Result saveOrUpdate(@Validated @RequestBody UserAddressDto userAddressDto) {
        return null;
    }

    @ApiOperation("收货地址列表详情")
    @PostMapping("/view")
    public Result<UserAddressDetailsVo> view(@Validated @RequestBody UserAddressDetailsDto userAddressDetailsDto) {
        return null;
    }

    @ApiOperation("删除收货地址")
    @PostMapping("/del")
    public Result queryUserAddressList(@Validated @RequestBody UserAddressDetailsDto userAddressDetailsDto) {
        return null;
    }

    @ApiOperation(value = "收件地址查询区域列表", httpMethod = "POST")
    @PostMapping("/queryCityRegion")
    public Result<List<CityRegionVo>> getCityRegionLis(@Validated @RequestBody CityRegionDto cityRegionDto) {
        return null;
    }
}
