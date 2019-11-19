package com.xingyun.bbc.mallpc.controller;


import com.alibaba.fastjson.JSON;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.detail.GoodsDetailMallDto;
import com.xingyun.bbc.mallpc.model.vo.detail.*;
import com.xingyun.bbc.mallpc.service.GoodDetailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Api("商品详情")
@RestController
@RequestMapping("/goodsDetail")
public class GoodsDetailController {

    public static final Logger logger = LoggerFactory.getLogger(GoodsDetailController.class);

    @Autowired
    private GoodDetailService goodDetailService;

    @ApiOperation(value = "获取商品主图", httpMethod = "GET")
    @GetMapping("/via/getGoodDetailPic")
    public Result<List<String>> getGoodDetailPic(@RequestParam Long fgoodsId, @RequestParam (required = false) Long fskuId){
        logger.info("获取商品主图入参 fgoodsId {} fskuId {}", fgoodsId, fskuId);
        return goodDetailService.getGoodDetailPic(fgoodsId, fskuId);
    }

    @ApiOperation(value = "获取商品基本信息", httpMethod = "GET")
    @GetMapping("/via/getGoodDetailBasic")
    public Result<GoodsVo> getGoodDetailBasic(@RequestParam Long fgoodsId, @RequestParam (required = false) Long fskuId){
        logger.info("获取商品基本信息 fgoodsId {} fskuId {}", fgoodsId, fskuId);
        return goodDetailService.getGoodDetailBasic(fgoodsId, fskuId);
    }

    @ApiOperation(value = "获取商品属性", httpMethod = "GET")
    @GetMapping("/via/getGoodsAttribute")
    public Result<Map<String, List<GoodsAttributeVo>>> getGoodsAttribute(@RequestParam Long fgoodsId){
        logger.info("获取商品属性 fgoodsId {} ", fgoodsId);
        return goodDetailService.getGoodsAttribute(fgoodsId);
    }

    @ApiOperation(value = "获取商品各种规格", httpMethod = "GET")
    @GetMapping("/via/getGoodsSpecifi")
    public Result<GoodspecificationVo> getGoodsSpecifi(@RequestParam Long fgoodsId){
        logger.info("获取商品规格 fgoodsId {} ", fgoodsId);
        return goodDetailService.getGoodsSpecifi(fgoodsId);
    }

    @ApiOperation(value = "获取价格", httpMethod = "POST")
    @PostMapping("/getGoodPrice")
    public Result<GoodsPriceVo> getGoodPrice(@RequestBody GoodsDetailMallDto goodsDetailMallDto, HttpServletRequest request){
        Long xyid = Long.parseLong(request.getHeader("xyid"));
        goodsDetailMallDto.setFuid(xyid);
//      goodsDetailMallDto.setFuid(1l);
        logger.info("获取价格 {}", JSON.toJSONString(goodsDetailMallDto));
        return goodDetailService.getGoodPrice(goodsDetailMallDto);
    }

    @ApiOperation(value = "获取库存", httpMethod = "POST")
    @PostMapping("/via/getGoodStock")
    public Result<GoodStockSellVo> getGoodStock(@RequestBody GoodsDetailMallDto goodsDetailMallDto){
        logger.info("获取库存 {}", JSON.toJSONString(goodsDetailMallDto));
        return goodDetailService.getGoodStock(goodsDetailMallDto);
    }

    @ApiOperation(value = "获取销量", httpMethod = "POST")
    @PostMapping("/via/getGoodSell")
    public Result<GoodStockSellVo> getGoodSell(@RequestBody GoodsDetailMallDto goodsDetailMallDto){
        logger.info("获取销量 {}", JSON.toJSONString(goodsDetailMallDto));
        return goodDetailService.getGoodSell(goodsDetailMallDto);
    }

    @ApiOperation(value = "获取是否加入常购清单", httpMethod = "GET")
    @GetMapping("/getIsRegular")
    public Result<Integer> getIsRegular(@RequestParam Long fgoodsId, HttpServletRequest request){
        Long xyid = Long.parseLong(request.getHeader("xyid"));
        logger.info("获取是否加入常购清单 fgoodsId {} fuid {}", fgoodsId, xyid);
        return goodDetailService.getIsRegular(fgoodsId, xyid);
    }

    @ApiOperation(value = "商品详情查询可领取优惠券--未点击", httpMethod = "GET")
    @GetMapping("/getSkuUserCouponLight")
    public Result<List<CouponVo>> getSkuUserCouponLight(@RequestParam Long fskuId, HttpServletRequest request){
        Long xyid = Long.parseLong(request.getHeader("xyid"));
        logger.info("商品详情查询可领取优惠券--未点击 fskuId {} fuid {}", fskuId, xyid);
        return goodDetailService.getSkuUserCouponLight(fskuId, xyid);
    }

    @ApiOperation(value = "商品详情查询可领取优惠券--点击", httpMethod = "GET")
    @GetMapping("/getSkuUserCoupon")
    public Result<GoodsDetailCouponVo> getSkuUserCoupon(@RequestParam Long fskuId, HttpServletRequest request){
        Long xyid = Long.parseLong(request.getHeader("xyid"));
        logger.info("商品详情查询可领取优惠券--点击 fskuId {} fuid {}", fskuId, xyid);
        return goodDetailService.getSkuUserCoupon(fskuId, xyid);
    }

    @ApiOperation(value = "获取优惠券使用说明", httpMethod = "GET")
    @GetMapping("/getCouponInstructions")
    public Result<String> getCouponInstructions(@RequestParam Long fcouponId, HttpServletRequest request){
        logger.info("获取优惠券使用说明 fcouponId {}, fcouponId");
        return goodDetailService.getCouponInstructions(fcouponId);
    }

    @ApiOperation(value = "商品详情领取优惠券", httpMethod = "GET")
    @GetMapping("/addReceiveCoupon")
    public Result<Boolean> addReceiveCoupon(@RequestParam Long fcouponId, HttpServletRequest request){
        Long xyid = Long.parseLong(request.getHeader("xyid"));
        logger.info("商品详情领取优惠券 fcouponId {} fuid {}", fcouponId, xyid);
        return goodDetailService.addReceiveCoupon(fcouponId, xyid);
    }

//    @ApiOperation(value = "获取sku批次有效期", httpMethod = "GET")
//    @GetMapping("/getSkuBatchSpecifi")
//    public Result<List<GoodsSkuBatchVo>> getSkuBatchSpecifi(@RequestParam Long fskuId){
//        return goodDetailService.getSkuBatchSpecifi(fskuId);
//    }
//
//    @ApiOperation(value = "获取sku批次包装规格", httpMethod = "GET")
//    @GetMapping("/getSkuBatchPackageSpecifi")
//    public Result<List<GoodsSkuBatchPackageVo>> getSkuBatchPackageSpecifi(@RequestParam Long fskuBatchId){
//        return goodDetailService.getSkuBatchPackageSpecifi(fskuBatchId);
//    }

}
