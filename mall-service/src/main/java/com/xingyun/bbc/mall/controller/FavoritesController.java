package com.xingyun.bbc.mall.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.JwtParser;
import com.xingyun.bbc.mall.base.utils.UserUtil;
import com.xingyun.bbc.mall.model.dto.GoodsPriceIntervalDto;
import com.xingyun.bbc.mall.model.vo.TokenInfoVo;
import com.xingyun.bbc.mall.service.FavoritesService;
import com.xingyun.bbc.order.api.FavoritesApi;
import com.xingyun.bbc.order.model.dto.cart.FavoritesClearDto;
import com.xingyun.bbc.order.model.dto.favorites.AddFavoritesDto;
import com.xingyun.bbc.order.model.dto.favorites.FavoritesDto;
import com.xingyun.bbc.order.model.vo.PageVo;
import com.xingyun.bbc.order.model.vo.favorites.FavoritesVo;
import com.xingyun.bbc.order.model.vo.favorites.SecondClassVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * @author hekaijin
 * @Description:
 * @createTime: 2019-09-06 13:16
 */
@Api("常购清单")
@RestController
@RequestMapping("/favorites")
@Validated
public class FavoritesController {

    @Autowired
    private FavoritesApi favoritesApi;
    @Resource
    private JwtParser jwtParser;
    @Autowired
    private FavoritesService favoritesService;

    @ApiOperation("移入常购清单")
    @PostMapping("/add")
    public Result<Boolean> addFavorites(@RequestBody @Valid AddFavoritesDto favoritesDto, HttpServletRequest request) {
        long fuid = UserUtil.uid(request);
        return favoritesApi.addFavorites(favoritesDto.setFuid(fuid)).setMsg("加入成功");
    }

    @ApiOperation("常购清单列表")
    @PostMapping("/list")
    public Result<PageVo<FavoritesVo>> favoritesList(@RequestBody FavoritesDto favoritesDto,HttpServletRequest request) {
        TokenInfoVo tokenInfo = jwtParser.getTokenInfo(request);
        GoodsPriceIntervalDto dto = new GoodsPriceIntervalDto();
        dto.setFuid(tokenInfo.getFuid().longValue());
        dto.setFverifyStatus(tokenInfo.getFverifyStatus());
        dto.setFoperateType(tokenInfo.getFoperateType());
        dto.setPageNum(favoritesDto.getPageNum());
        dto.setPageSize(favoritesDto.getPageSize());
        dto.setFcategoryId(favoritesDto.getFcategoryId());
        return Result.success(favoritesService.queryFavoritesPage(dto));
    }

    @ApiOperation("查询常购清单数量")
    @PostMapping("/count")
    public Result<Integer> countFavorites(HttpServletRequest request) {
        return favoritesApi.countFavorites(UserUtil.uid(request));
    }

    @ApiOperation("删除/清空常购清单")
    @PostMapping("/clear")
    public Result<Boolean> clear(@RequestBody @Valid FavoritesClearDto clearDto, HttpServletRequest request) {
        Long fuid = UserUtil.uid(request);
        return favoritesApi.clear(clearDto.setFuid(fuid)).setMsg("已移除清单");
    }

    @ApiOperation("常购清单分类")
    @PostMapping("/category")
    public Result<List<SecondClassVo>> category(HttpServletRequest request) {

        return favoritesApi.category(UserUtil.uid(request));
    }

}
