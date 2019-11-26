package com.xingyun.bbc.mallpc.controller;


import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.config.system.SystemConfig;
import com.xingyun.bbc.mallpc.model.vo.index.*;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemListVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemVo;
import com.xingyun.bbc.mallpc.service.IndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.*;


/**
 * @author chenxiang
 * @Description: 首页
 * @createTime: 2019-09-03 11:00
 */
@Validated
@Api("首页配置")
@RestController
@RequestMapping("/index")
@Slf4j
public class IndexController {
    @Autowired
    private IndexService indexService;

    @ApiOperation(value = "查询专题", httpMethod = "GET")
    @GetMapping("/via/getSpecialTopics")
    public Result<List<SpecialTopicVo>> getSpecialTopics() {
        return Result.success(indexService.getSpecialTopics());
    }


    @ApiOperation(value = "查询Banner", httpMethod = "GET")
    @GetMapping(value = "/via/getBanners")
    public Result<List<BannerVo>> getBanners() {
        return Result.success(indexService.getBanners());
    }

    @ApiOperation(value = "查询品牌", httpMethod = "GET")
    @GetMapping(value = "/via/getBrands")
    public Result<List<BrandVo>> getBrands(@RequestParam Long cateId) {
        return Result.success(indexService.getBrands(cateId));
    }

    @ApiOperation(value = "查询品牌", httpMethod = "GET")
    @GetMapping(value = "/via/getBrandList")
    public Result<List<CateBrandVo>> getBrandList(@RequestParam List<Long> cateIds) {
        Result<List<CateBrandVo>> result = Result.success(indexService.getBrandList(cateIds));
        //设置图片路径
        Map<String,Object> extra = new HashMap<>();
        extra.put("fdfsHost", StringUtils.join(SystemConfig.fdfsHost, File.separator));
        result.setExtra(extra);
        return result;
    }

    @ApiOperation(value = "查询分销商数量", httpMethod = "GET")
    @GetMapping(value = "/via/getUserCount")
    public Result<Integer> getUserCount() {
        return Result.success(indexService.getUserCount());
    }

    @ApiOperation(value = "商品分类查询接口")
    @GetMapping("/via/category")
    public Result<Set<GoodsCategoryVo>> queryCategories(){
        return indexService.queryCategories();
    }

}
