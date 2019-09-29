package com.xingyun.bbc.mall.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.vo.BrandListVo;
import com.xingyun.bbc.mall.model.vo.GoodsCategoryVo;
import com.xingyun.bbc.mall.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@Api("商品类目品牌")
@RestController
@RequestMapping("/category/via")
@Slf4j
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @ApiOperation(value = "查询商品品牌列表")
    @GetMapping("/queryBrandList")
    public Result<List<BrandListVo>> queryBrandList(@RequestParam(required = false) Long fcategoryId){
        log.info("查询品牌列表: "+Thread.currentThread() .getStackTrace()[1].getMethodName());
        return categoryService.queryBrandList(fcategoryId);
    }

    @ApiOperation(value = "查询商品类目树")
    @GetMapping("/queryCategoryList")
    public Result<List<GoodsCategoryVo>> queryCategoryList(HttpServletRequest request){
        log.info("查询商品类目树: "+Thread.currentThread() .getStackTrace()[1].getMethodName());
        return categoryService.queryCategoryTree();
    }

    @ApiOperation(value = "查询商品一级类目列表")
    @GetMapping("/queryGoodsCategoryList")
    public Result<List<GoodsCategoryVo>> queryGoodsCategoryList(HttpServletRequest request){
        log.info("查询商品一级类目列表: "+Thread.currentThread() .getStackTrace()[1].getMethodName());
        return categoryService.queryGoodsCategoryList();
    }

}
