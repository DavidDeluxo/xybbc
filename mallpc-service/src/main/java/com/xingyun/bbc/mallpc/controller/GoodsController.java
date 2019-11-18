package com.xingyun.bbc.mallpc.controller;


import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api("商品")
@Slf4j
@RestController
@RequestMapping("/goods")
public class GoodsController {


//    @Autowired
//    GoodsService goodsService;
//    @Autowired
//    UserService userService;
//    @Autowired
//    JwtParser jwtParser;
//    @Autowired
//    private CouponGoodsService couponGoodsService;
//
//    @ApiOperation("查询商品列表")
//    @PostMapping("/via/skuSearch")
//    public Result<SearchItemListVo<SearchItemVo>> skuSearch(@RequestBody SearchItemDto dto, HttpServletRequest request) {
//        TokenInfoVo infoVo = jwtParser.getTokenInfo(request);
//        dto.setIsLogin(infoVo.getIsLogin());
//        dto.setFuid(infoVo.getFuid());
//
//        log.info("查询商品列表,请求参数:{}", JSON.toJSONString(dto));
//        if (Objects.nonNull(dto.getFcouponId())) return couponGoodsService.queryGoodsList(dto);
//
//        return goodsService.searchSkuList(dto);
//    }
//
//    @ApiOperation("查询筛选信息")
//    @PostMapping("/via/skuSearchFilter")
//    public Result<SearchFilterVo> skuSearchFilter(@RequestBody SearchItemDto dto, HttpServletRequest request){
//        TokenInfoVo infoVo = jwtParser.getTokenInfo(request);
//        dto.setIsLogin(infoVo.getIsLogin());
//        dto.setFuid(infoVo.getFuid());
//
//        log.info("查询筛选信息,请求参数:{}", JSON.toJSONString(dto));
//        if (Objects.nonNull(dto.getFcouponId())) couponGoodsService.querySkuFilter(dto);
//
//        return goodsService.searchSkuFilter(dto);
//    }

}
