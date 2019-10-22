package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.vo.BrandListVo;
import com.xingyun.bbc.mall.model.vo.GoodsCategoryVo;

import java.util.List;

public interface CategoryService {

    Result<List<BrandListVo>> queryBrandList(Long fcategoryId);

    Result<List<GoodsCategoryVo>> queryCategoryTree();

    Result<List<GoodsCategoryVo>> queryGoodsCategoryList();

    Result<List<GoodsCategoryVo>> queryGoodsCategoryListNew();

    /**
     * 查询一级类目和热门推荐
     * @return
     */
    Result<List<GoodsCategoryVo>> queryCategoryLevelOne();

}
