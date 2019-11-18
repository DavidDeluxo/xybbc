package com.xingyun.bbc.mall.controller;


import com.alibaba.fastjson.JSON;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.JwtParser;
import com.xingyun.bbc.mall.model.dto.SearchItemDto;
import com.xingyun.bbc.mall.model.vo.*;
import com.xingyun.bbc.mall.service.CouponGoodsService;
import com.xingyun.bbc.mall.service.GoodsService;
import com.xingyun.bbc.mall.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

@Api("商品")
@Slf4j
@RestController
@RequestMapping("/goods")
public class GoodsController {


    @Autowired
    GoodsService goodsService;
    @Autowired
    UserService userService;
    @Autowired
    JwtParser jwtParser;
    @Autowired
    private CouponGoodsService couponGoodsService;

    @ApiOperation("查询商品列表")
    @PostMapping("/via/skuSearch")
    public Result<SearchItemListVo<SearchItemVo>> skuSearch(@RequestBody SearchItemDto dto, HttpServletRequest request) {
        TokenInfoVo infoVo = jwtParser.getTokenInfo(request);
        dto.setIsLogin(infoVo.getIsLogin());
        dto.setFuid(infoVo.getFuid());

        log.info("查询商品列表,请求参数:{}", JSON.toJSONString(dto));
        if (Objects.nonNull(dto.getFcouponId())) return couponGoodsService.queryGoodsList(dto);

        return goodsService.searchSkuList(dto);
    }

    @ApiOperation("查询筛选信息")
    @PostMapping("/via/skuSearchFilter")
    public Result<SearchFilterVo> skuSearchFilter(@RequestBody SearchItemDto dto, HttpServletRequest request){
        TokenInfoVo infoVo = jwtParser.getTokenInfo(request);
        dto.setIsLogin(infoVo.getIsLogin());
        dto.setFuid(infoVo.getFuid());
        return goodsService.searchSkuFilter(dto);
    }

    @ApiOperation(value = "查询品牌专属页")
    @PostMapping("/via/skuSearchBrandPage")
    public Result<BrandPageVo> skuSearchBrandPage(@ApiParam("品牌id") @RequestParam Integer fbrandId, HttpServletRequest request){
        return goodsService.searchSkuBrandPage(fbrandId);
    }

    @ApiOperation(value = "查询热门搜索")
    @PostMapping("/via/queryHotSearch")
    public Result<List<String>> queryHotSearch(){
        return goodsService.queryHotSearch();
    }

    @ApiOperation(value = "优惠券更新测试")
    @PostMapping("/via/updateCouponList")
    public Result<Boolean> updateCouponList(@RequestParam Long fcouponId){
        Coupon param = new Coupon();
        param.setFcouponId(fcouponId);
        goodsService.updateEsSkuWithCouponInfo(param);
        return Result.success(true);
    }



}
