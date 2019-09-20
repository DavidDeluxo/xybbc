package com.xingyun.bbc.mall.controller;


import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.SearchItemDto;
import com.xingyun.bbc.mall.model.vo.BrandPageVo;
import com.xingyun.bbc.mall.model.vo.PageVo;
import com.xingyun.bbc.mall.model.vo.SearchFilterVo;
import com.xingyun.bbc.mall.model.vo.SearchItemVo;
import com.xingyun.bbc.mall.service.GoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api("商品")
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    GoodsService goodsService;

    @ApiOperation("查询商品列表")
    @PostMapping("/skuSearch")
    public Result<PageVo<SearchItemVo>> skuSearch(@RequestBody SearchItemDto dto, HttpServletRequest request) {
        if(request.getHeader("xyid") != null){
            dto.setFuid(Integer.parseInt(request.getHeader("xyid")));
        }
        return goodsService.searchSkuList(dto);
    }

    @ApiOperation("查询筛选信息")
    @PostMapping("/skuSearchFilter")
    public Result<SearchFilterVo> skuSearchFilter(@RequestBody SearchItemDto dto, HttpServletRequest request){
        return goodsService.searchSkuFilter(dto);
    }

    @ApiOperation(value = "查询品牌专属页")
    @PostMapping("/skuSearchBrandPage")
    public Result<BrandPageVo> skuSearchBrandPage(@ApiParam("品牌id") @RequestParam Integer fbrandId, HttpServletRequest request){
        return goodsService.searchSkuBrandPage(fbrandId);
    }

    @ApiOperation(value = "查询热门搜索")
    @PostMapping("/queryHotSearch")
    public Result<List<String>> queryHotSearch(){
        return goodsService.queryHotSearch();
    }



}
