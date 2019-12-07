package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.mall.model.dto.GoodsPriceIntervalDto;
import com.xingyun.bbc.mall.model.vo.GoodsPriceIntervalVo;
import com.xingyun.bbc.order.model.vo.PageVo;
import com.xingyun.bbc.order.model.vo.favorites.FavoritesVo;

public interface FavoritesService {

    /**
     *  获取价格区间
     */
    GoodsPriceIntervalVo queryGoodPriceInterval(GoodsPriceIntervalDto priceIntervalDto);

    /**
     * 查询常购清单列表
     * @param goodsDetailMallDto
     * @return
     */
    PageVo<FavoritesVo> queryFavoritesPage(GoodsPriceIntervalDto goodsDetailMallDto);
}
