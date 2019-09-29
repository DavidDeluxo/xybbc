package com.xingyun.bbc.mall.controller;


import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.SearchItemDto;
import com.xingyun.bbc.mall.model.vo.BrandPageVo;
import com.xingyun.bbc.mall.model.vo.PageVo;
import com.xingyun.bbc.mall.model.vo.SearchFilterVo;
import com.xingyun.bbc.mall.model.vo.SearchItemVo;
import com.xingyun.bbc.mall.service.GoodsService;
import com.xingyun.bbc.search.api.MallSkuSearchApi;
import com.xingyun.bbc.search.criteria.EsCriteria;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Api("商品")
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    GoodsService goodsService;
    @Autowired
    MallSkuSearchApi mallSkuSearchApi;

    @ApiOperation("查询商品列表")
    @PostMapping("/via/skuSearch")
    public Result<PageVo<SearchItemVo>> skuSearch(@RequestBody SearchItemDto dto, HttpServletRequest request) {
        if(request.getHeader("xyId") != null){
            dto.setFuid(Integer.parseInt(request.getHeader("xyId")));
        }
        return goodsService.searchSkuList(dto);
    }

    @ApiOperation("查询筛选信息")
    @PostMapping("/via/skuSearchFilter")
    public Result<SearchFilterVo> skuSearchFilter(@RequestBody SearchItemDto dto, HttpServletRequest request){
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


}
