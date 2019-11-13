package com.xingyun.bbc.mall.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.JwtParser;
import com.xingyun.bbc.mall.model.dto.CouponGoodsDto;
import com.xingyun.bbc.mall.model.vo.SearchFilterVo;
import com.xingyun.bbc.mall.model.vo.SearchItemListVo;
import com.xingyun.bbc.mall.model.vo.SearchItemVo;
import com.xingyun.bbc.mall.model.vo.TokenInfoVo;
import com.xingyun.bbc.mall.service.CouponGoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


/**
 * @author hekaijin
 * @date 2019/11/11 11:07
 * @Description 优惠券商品
 */
@Slf4j
@Validated
@Api("优惠券商品")
@RestController
@RequestMapping("/coupon")
public class CouponGoodsController {

    @Autowired
    private CouponGoodsService couponGoodsService;
    @Autowired
    private JwtParser jwtParser;

    @ApiOperation("查询商品列表")
    @PostMapping("/goodsSearch")
    public Result<SearchItemListVo<SearchItemVo>> goodsSearch(@RequestBody @Valid CouponGoodsDto dto, HttpServletRequest request) {
        TokenInfoVo infoVo = jwtParser.getTokenInfo(request);
        dto.setIsLogin(infoVo.getIsLogin());
        dto.setFuid(infoVo.getFuid());
        return couponGoodsService.queryGoodsList(dto);
    }

    @ApiOperation("查询筛选信息")
    @PostMapping("/goodsSearchFilter")
    public Result<SearchFilterVo> goodsSearchFilter(@RequestBody @Valid CouponGoodsDto dto, HttpServletRequest request){
        TokenInfoVo infoVo = jwtParser.getTokenInfo(request);
        dto.setIsLogin(infoVo.getIsLogin());
        dto.setFuid(infoVo.getFuid());
        return couponGoodsService.querySkuFilter(dto);
    }

}
