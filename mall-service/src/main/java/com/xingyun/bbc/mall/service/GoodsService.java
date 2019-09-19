package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.SearchItemDto;
import com.xingyun.bbc.mall.model.vo.BrandPageVo;
import com.xingyun.bbc.mall.model.vo.PageVo;
import com.xingyun.bbc.mall.model.vo.SearchFilterVo;
import com.xingyun.bbc.mall.model.vo.SearchItemVo;

import java.util.List;
import java.util.Map;

public interface GoodsService {

    Result<PageVo<SearchItemVo>> searchSkuList(SearchItemDto searchItemDto);

    Result<SearchFilterVo> searchSkuFilter(SearchItemDto searchItemDto);

    Result<BrandPageVo> searchSkuBrandPage(Integer fbrandId);

    Result<Integer> insertSearchRecordAsync(String keyword, Integer fuid);

    Result<List<String>> queryHotSearch();

}
