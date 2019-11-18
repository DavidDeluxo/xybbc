package com.xingyun.bbc.mallpc.controller;


import com.alibaba.fastjson.JSON;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.JwtParser;
import com.xingyun.bbc.mallpc.model.dto.search.SearchItemDto;
import com.xingyun.bbc.mallpc.model.vo.search.SearchFilterVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemListVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemVo;
import com.xingyun.bbc.mallpc.service.GoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api("商品")
@Slf4j
@RestController
@RequestMapping("/goods")
public class GoodsController {


    @Autowired
    GoodsService goodsService;
    @Autowired
    JwtParser jwtParser;

    @ApiOperation("查询商品列表")
    @PostMapping("/skuSearch")
    public Result<SearchItemListVo<SearchItemVo>> skuSearch(@RequestBody SearchItemDto dto, HttpServletRequest request) {
//        TokenInfoVo infoVo = jwtParser.getTokenInfo(request);
//        dto.setIsLogin(infoVo.getIsLogin());
//        dto.setFuid(infoVo.getFuid());

        log.info("查询商品列表,请求参数:{}", JSON.toJSONString(dto));
//        if (Objects.nonNull(dto.getFcouponId())) return couponGoodsService.queryGoodsList(dto);

        return goodsService.searchSkuList(dto);
    }

    @ApiOperation("查询筛选信息")
    @PostMapping("/skuSearchFilter")
    public Result<SearchFilterVo> skuSearchFilter(@RequestBody SearchItemDto dto, HttpServletRequest request){
//        TokenInfoVo infoVo = jwtParser.getTokenInfo(request);
//        dto.setIsLogin(infoVo.getIsLogin());
//        dto.setFuid(infoVo.getFuid());

        log.info("查询筛选信息,请求参数:{}", JSON.toJSONString(dto));
//        if (Objects.nonNull(dto.getFcouponId())) couponGoodsService.querySkuFilter(dto);

        return goodsService.searchSkuFilter(dto);
    }

}
