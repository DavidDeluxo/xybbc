package com.xingyun.bbc.mall.service.impl;

import com.xingyun.bbc.common.redis.XyRedisManager;
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
import com.xingyun.bbc.mall.common.constans.MallRedisConstant;
import com.xingyun.bbc.mall.common.ensure.Ensure;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
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
    @Autowired
    XyRedisManager xyRedisManager;


    @Override
    public Result<List<GoodsCategoryVo>> queryCategoryLevelOne(){

        

        return null;
    }


    @Override
    public Result<List<BrandListVo>> queryBrandList(Long fcategoryId) {

        //根据类目筛选品牌
        List<Long> brandIdListFilter = null;
        if (fcategoryId != null) {
            Criteria<Goods, Object> goodsCriteria = Criteria.of(Goods.class)
                    .fields(Goods::getFbrandId)
                    .andEqualTo(Goods::getFcategoryId1, fcategoryId)
                    .andEqualTo(Goods::getFisDelete,0)
                    .andEqualTo(Goods::getFisDraft,0);
            Result<List<Goods>> goodsResult = goodsApi.queryByCriteria(goodsCriteria);
            if (!goodsResult.isSuccess()) {
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            if (CollectionUtils.isEmpty(goodsResult.getData())) {
                return Result.success();
            }
            List<Goods> goodsList = goodsResult.getData();
            brandIdListFilter = goodsList.stream().map(Goods::getFbrandId).distinct().collect(Collectors.toList());
        }

        //查询品牌列表
        Criteria<GoodsBrand, Object> brandCriteria = Criteria.of(GoodsBrand.class)
                //没有删除
                .andEqualTo(GoodsBrand::getFisDelete, 0)
                //显示
                .andEqualTo(GoodsBrand::getFisDisplay, 1);
        if(!CollectionUtils.isEmpty(brandIdListFilter)){
            //根据类目id筛选出来的品牌id
            brandCriteria.andIn(GoodsBrand::getFbrandId, brandIdListFilter);
        }
        brandCriteria.fields(GoodsBrand::getFbrandId, GoodsBrand::getFbrandName, GoodsBrand::getFbrandLogo, GoodsBrand::getFbrandSort, GoodsBrand::getFcreateTime);
        Result<List<GoodsBrand>> brandResult = goodsBrandApi.queryByCriteria(brandCriteria);
        Ensure.that(brandResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
        if (CollectionUtils.isEmpty(brandResult.getData())) {
            return Result.success(new LinkedList<>());
        }
        //转化为Vo
        List<GoodsBrand> brandList = brandResult.getData();
        List<BrandListVo> voList = new LinkedList<>();
        for (GoodsBrand brand : brandList) {
            BrandListVo brandListVo = new BrandListVo();
            BeanUtils.copyProperties(brand, brandListVo);
            voList.add(brandListVo);
        }
        //根据品牌名称排序
        voList = voList.stream().sorted(Comparator.comparing(BrandListVo::getFbrandName)).collect(Collectors.toList());
        return Result.success(voList);
    }

    @Override
    public Result<List<GoodsCategoryVo>> queryCategoryTree() {
        //首先查询缓存
        Object cachedCategory = xyRedisManager.get(MallRedisConstant.GOODS_CATEGORIES_CACHE);
        if(cachedCategory != null){
            List<GoodsCategoryVo> a = (List<GoodsCategoryVo>) cachedCategory;
            return Result.success(a);
        }

        //缓存无数据,查询数据库
        Result<List<GoodsCategory>> categoryResult = goodsCategoryApi.queryByCriteria(Criteria.of(GoodsCategory.class)
                .fields(GoodsCategory::getFcategoryId,
                        GoodsCategory::getFcategoryName,
                        GoodsCategory::getFcategoryDesc,
                        GoodsCategory::getFcategoryUrl,
                        GoodsCategory::getFparentCategoryId,
                        GoodsCategory::getFisRecommed,
                        GoodsCategory::getFcategorySort,
                        GoodsCategory::getFcreateTime)
                .andEqualTo(GoodsCategory::getFisDisplay, 1)
                .andEqualTo(GoodsCategory::getFisDelete, 0));
        if (!categoryResult.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if (CollectionUtils.isEmpty(categoryResult.getData())) {
            return Result.success(new LinkedList<>());
        }

        List<GoodsCategoryVo> goodsCategoryVoList = new LinkedList<>();
        for (GoodsCategory category : categoryResult.getData()) {
            GoodsCategoryVo categoryVo = new GoodsCategoryVo();
            BeanUtils.copyProperties(category, categoryVo);
            goodsCategoryVoList.add(categoryVo);
        }

        //以父类目id分组
        Map<Long, List<GoodsCategoryVo>> parentCategoriesMap = goodsCategoryVoList
                .parallelStream().collect(Collectors.groupingBy(GoodsCategoryVo::getFparentCategoryId, Collectors.toList()));
        //查询一级类目列表
        List<GoodsCategoryVo> level1Categories = goodsCategoryVoList
                .parallelStream().filter(category -> category.getFparentCategoryId() == 0).sorted().collect(Collectors.toList());
        this.fillChildrenCategoryList(level1Categories, parentCategoriesMap);

        //添加热门品牌
        Result<List<GoodsBrand>> hotBrandListResult = goodsBrandApi.queryByCriteria(Criteria.of(GoodsBrand.class)
                .andEqualTo(GoodsBrand::getFisDelete, 0)
                .andEqualTo(GoodsBrand::getFisDisplay, 1)
                .andEqualTo(GoodsBrand::getFisHot, 1));
        if (!hotBrandListResult.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        //一级类目添加热门品牌
        this.fillBrandsForL1Categories(level1Categories, hotBrandListResult.getData());

        //添加热门推荐
        GoodsCategoryVo hotRecommend = this.getHotRecommendCategory(goodsCategoryVoList, hotBrandListResult.getData());
        List<GoodsCategoryVo> resultList = new LinkedList<>();
        resultList.add(hotRecommend);
        resultList.addAll(level1Categories);
        //添加结果进缓存
        xyRedisManager.set(MallRedisConstant.GOODS_CATEGORIES_CACHE,resultList, 60 * 30);
        return Result.success(resultList);
    }

    private void fillBrandsForL1Categories(List<GoodsCategoryVo> level1Categories, List<GoodsBrand> hotBrands){

        for(GoodsCategoryVo l1Category : level1Categories){
            Criteria<Goods, Object> goodsCriteria = Criteria.of(Goods.class).andEqualTo(Goods::getFcategoryId1, l1Category.getFcategoryId());
            Result<List<Goods>> goodsResult = goodsApi.queryByCriteria(goodsCriteria);
            if (!goodsResult.isSuccess()) {
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            if (CollectionUtils.isEmpty(goodsResult.getData())) {
                continue;
            }
            List<Goods> goodsList = goodsResult.getData();
            List<Long> brandIdListFilter = goodsList.stream().map(Goods::getFbrandId).distinct().collect(Collectors.toList());
            List<GoodsBrand> goodsBrands = hotBrands.stream().filter(hotBrand -> brandIdListFilter.contains(hotBrand.getFbrandId())).collect(Collectors.toList());
            List<BrandListVo> hotBrandVoList = new LinkedList<>();
            for(GoodsBrand goodsBrand : goodsBrands){
                BrandListVo brandListVo = new BrandListVo();
                BeanUtils.copyProperties(goodsBrand, brandListVo);
                hotBrandVoList.add(brandListVo);
            }
            l1Category.setHotBrandList(hotBrandVoList);
        }

    }

    private GoodsCategoryVo getHotRecommendCategory(List<GoodsCategoryVo> goodsCategories, List<GoodsBrand> hotBrands) {
        GoodsCategoryVo hotRecommend = new GoodsCategoryVo();
        hotRecommend.setFcategoryName("热门推荐");
        hotRecommend.setFcategoryId(0L);
        //热门类目
        List<GoodsCategoryVo> l2RecommendList = new LinkedList<>();
        for (GoodsCategoryVo recommendL1 : goodsCategories) {
            if (!CollectionUtils.isEmpty(recommendL1.getChildrenList())) {
                for (GoodsCategoryVo l2Vo : recommendL1.getChildrenList()) {
                    if (!CollectionUtils.isEmpty(l2Vo.getChildrenList())) {
                        List<GoodsCategoryVo> l3VoList = l2Vo.getChildrenList();
                        List<GoodsCategoryVo> l3RecommendList = l3VoList.stream().filter(l3Category->l3Category.getFisRecommed() == 1).collect(Collectors.toList());
                        if(!CollectionUtils.isEmpty(l3RecommendList)){
                            l2RecommendList.add(l2Vo);
                        }
                    }
                }
            }
        }
        hotRecommend.setChildrenList(l2RecommendList);

        List<BrandListVo> hotBrandList = new LinkedList<>();
        if (!CollectionUtils.isEmpty(hotBrands)) {
            for (GoodsBrand hotBrand : hotBrands) {
                BrandListVo brandVo = new BrandListVo();
                BeanUtils.copyProperties(hotBrand, brandVo);
                hotBrandList.add(brandVo);
            }
        }
        Collections.sort(hotBrandList);
        hotRecommend.setHotBrandList(hotBrandList);
        return hotRecommend;
    }

    @Override
    public Result<List<GoodsCategoryVo>> queryGoodsCategoryListNew(){
        List<GoodsCategoryVo> categoryVoList = new LinkedList<>();
        //查询一级类目列表
        Result<List<GoodsCategory>> categoryListResultAll = goodsCategoryApi.queryByCriteria(Criteria.of(GoodsCategory.class)
                .fields(GoodsCategory::getFcategoryId,
                        GoodsCategory::getFcategoryName,
                        GoodsCategory::getFcategoryDesc,
                        GoodsCategory::getFcategorySort,
                        GoodsCategory::getFmodifyTime)
                //一级类目父类目id为0
                .andEqualTo(GoodsCategory::getFparentCategoryId,0)
                //类目未删除
                .andEqualTo(GoodsCategory::getFisDelete, 0)
                //类目展示
                .andEqualTo(GoodsCategory::getFisDisplay, 1)
                //修改时间倒序
                .sortDesc(GoodsCategory::getFcategorySort));
        Ensure.that(categoryListResultAll.isSuccess()).isTrue(MallExceptionCode.PARAM_ERROR);
        if(CollectionUtils.isEmpty(categoryListResultAll.getData())){
            return Result.success(categoryVoList);
        }

        //过滤没有商品的一级类目
        List<GoodsCategory> filteredCategories = new LinkedList<>();
        for (GoodsCategory category : categoryListResultAll.getData()){
           Result<Integer> countResult = goodsApi.countByCriteria(Criteria.of(Goods.class).andEqualTo(Goods::getFcategoryId1, category.getFcategoryId()));
           Ensure.that(countResult).isNotNull(MallExceptionCode.SYSTEM_ERROR);
           if(countResult.getData() > 0){
                filteredCategories.add(category);
           }
        }
        //转化Vo
        if (!CollectionUtils.isEmpty(filteredCategories)) {
            for (GoodsCategory category : categoryListResultAll.getData()) {
                GoodsCategoryVo categoryVo = new GoodsCategoryVo();
                categoryVo.setFcategoryId(category.getFcategoryId());
                categoryVo.setFcategoryName(category.getFcategoryName());
                categoryVo.setFcategoryDesc(category.getFcategoryDesc());
                categoryVo.setFcategorySort(category.getFcategorySort());
                categoryVo.setFmodifyTime(category.getFmodifyTime());
                categoryVoList.add(categoryVo);
            }
        }
        categoryVoList = categoryVoList.stream().sorted().collect(Collectors.toList());
        return Result.success(categoryVoList);
    }


    @Override
    public Result<List<GoodsCategoryVo>> queryGoodsCategoryList() {
        List<GoodsCategoryVo> categoryVoList = new LinkedList<>();
        Result<List<Goods>> goodsResultAll = goodsApi.queryAll();
        if (!goodsResultAll.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if (!CollectionUtils.isEmpty(goodsResultAll.getData())) {
            List<Goods> goodsListAll = goodsResultAll.getData();
            List<Long> categoryListFiltered = goodsListAll.stream().map(Goods::getFcategoryId1).distinct().collect(Collectors.toList());
            Result<List<GoodsCategory>> categoryListResultAll = goodsCategoryApi.queryByCriteria(Criteria.of(GoodsCategory.class)
                    .fields(GoodsCategory::getFcategoryId, GoodsCategory::getFcategoryName, GoodsCategory::getFcategoryDesc)
                    .andIn(GoodsCategory::getFcategoryId, categoryListFiltered)
                    .andEqualTo(GoodsCategory::getFisDelete, 0)
                    .andEqualTo(GoodsCategory::getFisDisplay, 1)
                    .sortDesc(GoodsCategory::getFmodifyTime));
            if (!categoryListResultAll.isSuccess()) {
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            if (!CollectionUtils.isEmpty(categoryListResultAll.getData())) {
                for (GoodsCategory category : categoryListResultAll.getData()) {
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
     *
     * @param categories
     * @param categoriesMap
     */
    private void fillChildrenCategoryList(List<GoodsCategoryVo> categories, Map<Long, List<GoodsCategoryVo>> categoriesMap) {
        for (GoodsCategoryVo vo : categories) {
            List<GoodsCategoryVo> childrenList = categoriesMap.get(vo.getFcategoryId());
            if (CollectionUtils.isEmpty(childrenList)) {
                continue;
            }
            Collections.sort(childrenList);
            this.fillChildrenCategoryList(childrenList, categoriesMap);
            vo.setChildrenList(childrenList);
        }
    }

}
