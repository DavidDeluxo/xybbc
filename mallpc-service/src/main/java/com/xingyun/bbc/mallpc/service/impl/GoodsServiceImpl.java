package com.xingyun.bbc.mallpc.service.impl;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.search.SearchItemDto;
import com.xingyun.bbc.mallpc.model.vo.search.SearchFilterVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemListVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemVo;
import com.xingyun.bbc.mallpc.service.GoodsService;
import org.springframework.stereotype.Service;

@Service
public class GoodsServiceImpl implements GoodsService {

    /**
     * 查询商品列表
     * @param searchItemDto
     * @return
     */
    @Override
    public Result<SearchItemListVo<SearchItemVo>> searchSkuList(SearchItemDto searchItemDto) {
        return null;
    }

    /**
     * 查询商品筛选信息列表
     * @param searchItemDto
     * @return
     */
    @Override
    public Result<SearchFilterVo> searchSkuFilter(SearchItemDto searchItemDto) {
        return null;
    }
}
