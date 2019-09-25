package com.xingyun.bbc.mall.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.UserUtil;
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

    @ApiOperation("移入常购清单")
    @PostMapping("/add")
    public Result<Boolean> addFavorites(@RequestBody @Valid AddFavoritesDto favoritesDto, HttpServletRequest request) {
        long fuid = UserUtil.uid(request);
        return favoritesApi.addFavorites(favoritesDto.setFuid(fuid));
    }

    @ApiOperation("常购清单列表")
    @PostMapping("/list")
    public Result<PageVo<FavoritesVo>> favoritesList(@RequestBody FavoritesDto favoritesDto,HttpServletRequest request) {
        long fuid = UserUtil.uid(request);
        return favoritesApi.favoritesList(favoritesDto.setFuid(fuid));
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
        return favoritesApi.clear(clearDto.setFuid(fuid));
    }

    @ApiOperation("常购清单分类")
    @PostMapping("/category")
    public Result<List<SecondClassVo>> category(HttpServletRequest request) {

        return favoritesApi.category(UserUtil.uid(request));
    }

}
