package com.xingyun.bbc.mall.controller;


import com.xingyun.bbc.common.jwt.XyUserJwtManager;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.model.dto.*;
import com.xingyun.bbc.mall.model.vo.*;
import com.xingyun.bbc.mall.service.CategoryService;
import com.xingyun.bbc.mall.service.IndexService;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;



/**
 * @author lll
 * @Description: 首页
 * @createTime: 2019-09-03 11:00
 */
@Validated
@Api("首页配置")
@RestController
@RequestMapping("/index")
@Slf4j
public class IndexController {
    public static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    @Autowired
    private IndexService indexService;

    @Autowired
    CategoryService categoryService;

    @Resource
    private DozerHolder holder;

    @Autowired
    private XyUserJwtManager userJwtManager;

    private static final String ACCESS_TOKEN = "accessToken";

    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", dataType = "Integer", name = "fposition", value = "导航栏位置(0Banner配置 1ICON配置 2专题位配置", required = false)})
    @ApiOperation(value = "查询首页配置", httpMethod = "POST")
    @PostMapping("/via/getConfig")
    //@PostMapping("/getConfig")
    public Result<List<PageConfigVo>> getConfig(@RequestParam(value = "fposition", required = true) Integer fposition) {
        return indexService.getConfig(fposition);
    }

    @ApiOperation(value = "查询商品一级类目列表")
    @GetMapping("/via/queryGoodsCategoryList")
    //@GetMapping("/queryGoodsCategoryList")
    public Result<List<IndexGoodsCategoryVo>> queryGoodsCategoryList(HttpServletRequest request) {
        log.info("查询商品一级类目列表: " + Thread.currentThread().getStackTrace()[1].getMethodName());
        Result<List<GoodsCategoryVo>> result = indexService.queryGoodsCategoryList();
        List<GoodsCategoryVo> list = result.getData();
        return Result.success(holder.convert(list, IndexGoodsCategoryVo.class));
    }
    
    @ApiOperation(value="引导页启动页查询",httpMethod = "POST")
    @ApiResponses({@ApiResponse(code = 200, response = GuidePageVo.class, message = "操作成功!") })
    @PostMapping(value = "/selectGuidePage",consumes = MediaType.APPLICATION_JSON_VALUE)
    public  Result<List<GuidePageVo>> selectGuidePageVos(@ApiParam(name = "引导页启动页查询", value = "传入json格式", required = false) @RequestBody GuidePageDto guidePageDto){
        return indexService.selectGuidePageVos(guidePageDto.getFtype());
    }


    @ApiOperation(value = "查询一级类目下所有sku列表---不校验登录")
    @PostMapping("/via/queryGoodsByCategoryId1")
    //@PostMapping("/queryGoodsByCategoryId1")
    public Result<PageVo<IndexSkuGoodsVo>> queryGoodsByCategoryId1(CategoryDto categoryDto, HttpServletRequest request) {
        this.getTokenInfo(categoryDto, request);
        return Result.success(indexService.queryGoodsByCategoryId1(categoryDto));
    }


/*    @ApiOperation(value = "查询一级类目下所有sku列表---校验登录")
    @PostMapping("/queryGoodsByCategoryId1OnLogin")
    public Result<PageVo<IndexSkuGoodsVo>> queryGoodsByCategoryId1OnLogin(CategoryDto categoryDto, HttpServletRequest request) {
        categoryDto.setFuid(Long.parseLong(request.getHeader("xyid")));
        //categoryDto.setFuid(Long.valueOf(10157));
        return Result.success(indexService.queryGoodsByCategoryId1(categoryDto));
    }*/

    private void getTokenInfo(CategoryDto dto, HttpServletRequest request){
        if(StringUtils.isNotEmpty(request.getHeader(ACCESS_TOKEN))){
            String token = request.getHeader(ACCESS_TOKEN);
            Claims claims = userJwtManager.parseJwt(token);
            if (claims == null) {
                dto.setIsLogin(false);
                return;
            }
            String id = claims.getId();
            dto.setIsLogin(true);
            dto.setFuid(Long.valueOf(Integer.parseInt(id)));
        }
    }


}
