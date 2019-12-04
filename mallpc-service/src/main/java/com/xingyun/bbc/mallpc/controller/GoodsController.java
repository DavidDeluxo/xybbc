package com.xingyun.bbc.mallpc.controller;

import com.alibaba.fastjson.JSON;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.utils.JwtParser;
import com.xingyun.bbc.mallpc.config.system.SystemConfig;
import com.xingyun.bbc.mallpc.model.dto.search.SearchItemDto;
import com.xingyun.bbc.mallpc.model.vo.TokenInfoVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchFilterVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemListVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemVo;
import com.xingyun.bbc.mallpc.service.CouponGoodsService;
import com.xingyun.bbc.mallpc.service.GoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Api("商品")
@Slf4j
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private CouponGoodsService couponGoodsService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private JwtParser jwtParser;

    @ApiOperation("查询商品列表")
    @PostMapping("/via/skuSearch")
    public Result<SearchItemListVo<SearchItemVo>> skuSearch(@RequestBody SearchItemDto dto, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        TokenInfoVo infoVo = jwtParser.getTokenInfo(request);
        dto.setIsLogin(infoVo.getIsLogin());
        dto.setFuid(infoVo.getFuid());
        dto.setFverifyStatus(infoVo.getFverifyStatus());
        dto.setFoperateType(infoVo.getFoperateType());
        log.info("查询商品列表,请求参数:{}", JSON.toJSONString(dto));

        Result<SearchItemListVo<SearchItemVo>> result;
        if (Objects.nonNull(dto.getFcouponId())) {
            result = couponGoodsService.queryGoodsList(dto);
        } else {
            result = goodsService.searchSkuList(dto);
        }
        Map<String, Object> extra = new HashMap<>();
        extra.put("fdfsHost", StringUtils.join(SystemConfig.fdfsHost, File.separator));
        result.setExtra(extra);
        log.info("查询商品列表,请求耗时:{}", (System.currentTimeMillis() - start));
        return result;
    }

    @ApiOperation("查询筛选信息")
    @PostMapping("/via/skuSearchFilter")
    public Result<SearchFilterVo> skuSearchFilter(@RequestBody SearchItemDto dto, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        TokenInfoVo infoVo = jwtParser.getTokenInfo(request);
        dto.setIsLogin(infoVo.getIsLogin());
        dto.setFuid(infoVo.getFuid());
        log.info("查询筛选信息,请求参数:{}", JSON.toJSONString(dto));
        Result<SearchFilterVo> result = goodsService.searchSkuFilter(dto);
        log.info("查询筛选信息,请求耗时:{}", (System.currentTimeMillis() - start));
        return result;
    }

}
