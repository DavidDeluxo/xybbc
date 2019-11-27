package com.xingyun.bbc.mallpc.service;


import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.search.SearchItemDto;
import com.xingyun.bbc.mallpc.model.vo.TokenInfoVo;
import com.xingyun.bbc.mallpc.model.vo.index.CateSearchItemListVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchFilterVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemListVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GoodsService {

    /**
     * 查询商品列表
     * @param searchItemDto
     * @return
     */
    Result<SearchItemListVo<SearchItemVo>> searchSkuList(SearchItemDto searchItemDto);

    /**
     * 查询商品筛选信息列表
     * @param searchItemDto
     * @return
     */
    Result<SearchFilterVo> searchSkuFilter(SearchItemDto searchItemDto);

    /**
     * 首页楼层一级分类热销前20数据
     * @param cateIds
     * @param infoVo
     * @return
     */
    Result<List<CateSearchItemListVo>> floorSkus(List<Integer> cateIds, TokenInfoVo infoVo);


}
