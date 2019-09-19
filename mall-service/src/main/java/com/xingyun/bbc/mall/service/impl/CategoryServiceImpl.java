package com.xingyun.bbc.mall.service.impl;

import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.GoodsApi;
import com.xingyun.bbc.core.sku.api.GoodsBrandApi;
import com.xingyun.bbc.core.sku.api.GoodsCategoryApi;
import com.xingyun.bbc.core.sku.po.Goods;
import com.xingyun.bbc.core.sku.po.GoodsBrand;
import com.xingyun.bbc.core.sku.po.GoodsCategory;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.vo.BrandListVo;
import com.xingyun.bbc.mall.model.vo.GoodsCategoryVo;
import com.xingyun.bbc.mall.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    GoodsCategoryApi goodsCategoryApi;
    @Autowired
    GoodsApi goodsApi;
    @Autowired
    GoodsBrandApi goodsBrandApi;

    @Override
    public Result<List<BrandListVo>> queryBrandList(Long fcategoryId){



        Result<List<GoodsBrand>> brandResult = goodsBrandApi.queryByCriteria(Criteria.of(GoodsBrand.class).andEqualTo(GoodsBrand::getFisDelete,0));
        if(!brandResult.isSuccess()){
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if(CollectionUtils.isEmpty(brandResult.getData())){
            return Result.success(new LinkedList<>());
        }

        List<GoodsBrand> brandList = brandResult.getData();

        //根据类目筛选品牌
        if(fcategoryId != null){
            Criteria<Goods,Object> goodsCriteria = Criteria.of(Goods.class);
            goodsCriteria.andEqualTo(Goods::getFcategoryId1, fcategoryId);
            Result<List<Goods>> goodsResult = goodsApi.queryByCriteria(goodsCriteria);
            if(!goodsResult.isSuccess()){
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            if(CollectionUtils.isEmpty(goodsResult.getData())){
                return Result.success();
            }
            List<Goods> goodsList = goodsResult.getData();
            List<Long> brandIdListFilter = goodsList.stream().map(Goods::getFbrandId).distinct().collect(Collectors.toList());
            brandList = brandList.stream().filter(s->brandIdListFilter.contains(s.getFbrandId())).collect(Collectors.toList());
        }

        List<BrandListVo> voList = new LinkedList<>();
        for(GoodsBrand brand : brandList){
            BrandListVo brandListVo = new BrandListVo();
            BeanUtils.copyProperties(brand, brandListVo);
            voList.add(brandListVo);
        }

        voList = voList.stream().sorted(Comparator.comparing(BrandListVo::getFbrandName)).collect(Collectors.toList());

        List<GoodsCategoryVo> categoryVoList = new LinkedList<>();
        Result<List<Goods>> goodsResultAll = goodsApi.queryAll();
        if(!goodsResultAll.isSuccess()){
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if(!CollectionUtils.isEmpty(goodsResultAll.getData())){
            List<Goods> goodsListAll = goodsResultAll.getData();
            List<Long> categoryListFiltered = goodsListAll.stream().map(Goods::getFcategoryId1).distinct().collect(Collectors.toList());
            Result<List<GoodsCategory>> categoryListResultAll = goodsCategoryApi.queryByCriteria(Criteria.of(GoodsCategory.class).andIn(GoodsCategory::getFcategoryId , categoryListFiltered));
            if(!categoryListResultAll.isSuccess()){
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            if(!CollectionUtils.isEmpty(categoryListResultAll.getData())){

                for(GoodsCategory category : categoryListResultAll.getData()){
                    GoodsCategoryVo categoryVo = new GoodsCategoryVo();
                    categoryVo.setFcategoryId(category.getFcategoryId());
                    categoryVo.setFcategoryName(category.getFcategoryName());
                    categoryVoList.add(categoryVo);
                }

            }
        }
        Map<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("categoryList",categoryVoList);
        return Result.success(voList).setExtra(categoryMap);
    }

    @Override
    public Result<List<GoodsCategoryVo>> queryCategoryTree(){
        Result<List<GoodsCategory>> categoryResult = goodsCategoryApi.queryByCriteria(Criteria.of(GoodsCategory.class)
                .andEqualTo(GoodsCategory::getFisDelete,0));
        if(!categoryResult.isSuccess()){
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if(CollectionUtils.isEmpty(categoryResult.getData())){
            return Result.success(new LinkedList<>());
        }

        List<GoodsCategoryVo> goodsCategoryVoList = new LinkedList<>();
        for(GoodsCategory category : categoryResult.getData()){
            GoodsCategoryVo categoryVo = new GoodsCategoryVo();
            BeanUtils.copyProperties(category, categoryVo);
            goodsCategoryVoList.add(categoryVo);
        }

        //以父类目id分组
        Map<Long, List<GoodsCategoryVo>> parentCategoriesMap = goodsCategoryVoList
                .parallelStream().collect(Collectors.groupingBy(GoodsCategoryVo::getFparentCategoryId, Collectors.toList()));
        //查询一级类目列表
        List<GoodsCategoryVo> level1Categories = goodsCategoryVoList
                .parallelStream().filter(category->category.getFparentCategoryId() == 0).sorted().collect(Collectors.toList());
        this.fillChildrenCategoryList(level1Categories, parentCategoriesMap);
        return Result.success(level1Categories);
    }

    @Override
    public Result<List<GoodsCategoryVo>> queryGoodsCategoryList(){
        List<GoodsCategoryVo> categoryVoList = new LinkedList<>();
        Result<List<Goods>> goodsResultAll = goodsApi.queryAll();
        if(!goodsResultAll.isSuccess()){
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if(!CollectionUtils.isEmpty(goodsResultAll.getData())){
            List<Goods> goodsListAll = goodsResultAll.getData();
            List<Long> categoryListFiltered = goodsListAll.stream().map(Goods::getFcategoryId1).distinct().collect(Collectors.toList());
            Result<List<GoodsCategory>> categoryListResultAll = goodsCategoryApi.queryByCriteria(Criteria.of(GoodsCategory.class).andIn(GoodsCategory::getFcategoryId , categoryListFiltered).sortDesc(GoodsCategory::getFmodifyTime));
            if(!categoryListResultAll.isSuccess()){
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            if(!CollectionUtils.isEmpty(categoryListResultAll.getData())){

                for(GoodsCategory category : categoryListResultAll.getData()){
                    GoodsCategoryVo categoryVo = new GoodsCategoryVo();
                    categoryVo.setFcategoryId(category.getFcategoryId());
                    categoryVo.setFcategoryName(category.getFcategoryName());
                    categoryVo.setFcategoryDesc(category.getFcategoryDesc());
                    categoryVoList.add(categoryVo);
                }

            }
        }
        return Result.success(categoryVoList);
    }

    /**
     * 填充子类目列表
     * @param categories
     * @param categoriesMap
     */
    private void fillChildrenCategoryList(List<GoodsCategoryVo> categories, Map<Long, List<GoodsCategoryVo>> categoriesMap){
        for (GoodsCategoryVo vo : categories){
            List<GoodsCategoryVo> childrenList = categoriesMap.get(vo.getFcategoryId());
            if(CollectionUtils.isEmpty(childrenList)){
                continue;
            }
            Collections.sort(childrenList);
            this.fillChildrenCategoryList(childrenList, categoriesMap);
            vo.setChildrenList(childrenList);
        }
    }

}
