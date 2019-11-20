package com.xingyun.bbc.mallpc.controller;


import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.vo.index.BannerVo;
import com.xingyun.bbc.mallpc.model.vo.index.BrandVo;
import com.xingyun.bbc.mallpc.model.vo.index.SpecialTopicVo;
import com.xingyun.bbc.mallpc.service.IndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


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
    @GetMapping("/getSpecialTopics")
    public Result<List<SpecialTopicVo>> getSpecialTopics() {
        return Result.success(indexService.getSpecialTopics());
    }


    @ApiOperation(value = "查询Banner", httpMethod = "GET")
    @GetMapping(value = "/getBanners")
    public Result<List<BannerVo>> getBanners() {
        return Result.success(indexService.getBanners());
    }

    @ApiOperation(value = "查询品牌", httpMethod = "GET")
    @GetMapping(value = "/getBrands")
    public Result<List<BrandVo>> getBrands(@RequestParam Long cateId) {
        return Result.success(indexService.getBrands(cateId));
    }

    @ApiOperation(value = "查询分销商数量", httpMethod = "GET")
    @GetMapping(value = "/getUserCount")
    public Result<Integer> getUserCount() {
        return Result.success(indexService.getUserCount());
    }

}
