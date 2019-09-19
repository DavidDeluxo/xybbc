package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.vo.BrandListVo;
import com.xingyun.bbc.mall.model.vo.GoodsCategoryVo;

import java.util.List;

public interface CategoryService {

    Result<List<BrandListVo>> queryBrandList(Long fcategoryId);

    Result<List<GoodsCategoryVo>> queryCategoryTree();

    Result<List<GoodsCategoryVo>> queryGoodsCategoryList();
}
