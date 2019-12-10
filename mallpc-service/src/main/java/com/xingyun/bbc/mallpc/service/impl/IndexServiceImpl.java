package com.xingyun.bbc.mallpc.service.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.xingyun.bbc.core.operate.api.PageConfigApi;
import com.xingyun.bbc.core.operate.enums.BooleanNum;
import com.xingyun.bbc.core.operate.enums.GuideConfigType;
import com.xingyun.bbc.core.operate.enums.PageConfigPositionEnum;
import com.xingyun.bbc.core.operate.po.PageConfig;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.GoodsApi;
import com.xingyun.bbc.core.sku.api.GoodsBrandApi;
import com.xingyun.bbc.core.sku.api.GoodsCategoryApi;
import com.xingyun.bbc.core.sku.po.Goods;
import com.xingyun.bbc.core.sku.po.GoodsBrand;
import com.xingyun.bbc.core.sku.po.GoodsCategory;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.FileUtils;
import com.xingyun.bbc.mallpc.common.utils.MD5Util;
import com.xingyun.bbc.mallpc.common.utils.ResultUtils;
import com.xingyun.bbc.mallpc.model.vo.index.*;
import com.xingyun.bbc.mallpc.service.IndexService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class IndexServiceImpl implements IndexService {

    /**
     * 首页一级分类下最多展示的品牌数
     */
    private static final int BRAND_MAX = 6;

    /**
     * 首页一级分类下热门品牌缓存有效期半小时
     */
    private static final long BRAND_EXPIRE = 1800;

    /**
     * 首页用户总数缓存有效期2分钟
     */
    private static final long USER_COUNT_EXPIRE = 120;

    @Resource
    private UserApi userApi;
    @Resource
    private PageConfigApi pageConfigApi;
    @Resource
    private GoodsBrandApi goodsBrandApi;
    @Resource
    private GoodsApi goodsApi;
    @Resource
    private CacheTemplate cacheTemplate;
    @Resource
    private DozerHolder dozerHolder;

    @Resource
    private GoodsCategoryApi goodsCategoryApi;

    @Override
    public List<SpecialTopicVo> getSpecialTopics() {
        List<PageConfig> result = (List<PageConfig>) cacheTemplate
                .get(PC_MALL_PAGECONFIG_TOPIC, PC_MALL_PAGECONFIG_TOPIC_UPDATE, null, () -> getPageConfig(PageConfigPositionEnum.SPECIAL_TOPIC.getKey()));
        //若配置中的relationId是默认值0，置为null不返回前端
        setNullIfZero(result);
        List<SpecialTopicVo> vos = dozerHolder.convert(result, SpecialTopicVo.class);
        vos.forEach(vo -> vo.setFimgUrl(FileUtils.getFileUrl(vo.getFimgUrl())));
        return vos;
    }

    @Override
    public List<BannerVo> getBanners() {
        List<PageConfig> result = (List<PageConfig>) cacheTemplate
                .get(PC_MALL_PAGECONFIG_BANNER, PC_MALL_PAGECONFIG_BANNER_UPDATE, null, () -> getPageConfig(PageConfigPositionEnum.BANNER.getKey()));
        //若配置中的relationId是默认值0，置为null不返回前端
        setNullIfZero(result);
        List<BannerVo> vos = dozerHolder.convert(result, BannerVo.class);
        vos.forEach(vo -> vo.setFimgUrl(FileUtils.getFileUrl(vo.getFimgUrl())));
        return vos;
    }

    @Override
    public Integer getUserCount() {
        return (Integer) cacheTemplate
                .get(USER_COUNT, USER_COUNT_LOCK, null, () -> ResultUtils.getData(userApi.count(new User())));
    }

    @Override
    public List<CateBrandVo> getBrandList(List<Long> cateIds) {
        String key;
        if (CollectionUtils.isEmpty(cateIds)) {
            key = "";
        } else {
            key = MD5Util.toMd5(Joiner.on("").join(cateIds));
        }
        List<CateBrandVo> result = (List<CateBrandVo>) cacheTemplate
                .get(INDEX_BRAND + key, INDEX_BRAND_UPDATE + key, BRAND_EXPIRE, () -> getCateBrands(cateIds));
        return result;
    }

    /**
     * 查询一级分类下的热门品牌
     *
     * @param cateIds
     * @return
     */
    private List<CateBrandVo> getCateBrands(List<Long> cateIds) {
        //查询热门品牌
        Criteria<GoodsBrand, Object> brandCriteria = Criteria.of(GoodsBrand.class)
                .andEqualTo(GoodsBrand::getFisDelete, 0)
                .andEqualTo(GoodsBrand::getFisDisplay, 1)
                .andEqualTo(GoodsBrand::getFisHot, 1);
        List<GoodsBrand> brandList = ResultUtils.getData(goodsBrandApi.queryByCriteria(brandCriteria));
        if (CollectionUtils.isEmpty(brandList)) {
            return new ArrayList();
        }

        List<CateBrandVo> list = cateIds.stream().map(item -> {
            CateBrandVo vo = new CateBrandVo();
            vo.setCateId(item);
            return vo;
        }).collect(toList());

        Criteria<Goods, Object> goodsCriteria = Criteria.of(Goods.class)
                .fields(Goods::getFbrandId, Goods::getFcategoryId1);
        List<Goods> goodies = ResultUtils.getData(goodsApi.queryByCriteria(goodsCriteria));
        Map<Long, List<Goods>> cateGoodsMap = goodies.stream().collect(Collectors.groupingBy((Goods::getFcategoryId1)));
        for (CateBrandVo vo : list) {
            //查询入参一级分类id下的Goods的brandIds
            List<Goods> goodsList = cateGoodsMap.get(vo.getCateId());
            if (CollectionUtils.isEmpty(goodsList)) {
                vo.setBrands(new ArrayList<>());
                continue;
            }
            List<Long> brandIds = goodsList.stream().map(Goods::getFbrandId).distinct().collect(Collectors.toList());

            //筛选出符合条件的品牌信息
            List<GoodsBrand> goodsBrands = brandList.stream().filter(hotBrand -> brandIds.contains(hotBrand.getFbrandId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(goodsBrands)) {
                vo.setBrands(new ArrayList<>());
                continue;
            }

            int endIndex = goodsBrands.size() > BRAND_MAX ? BRAND_MAX : goodsBrands.size();
            List<GoodsBrand> brands = goodsBrands.subList(0, endIndex);
            vo.setBrands(dozerHolder.convert(brands, BrandVo.class));
        }
        return list;
    }

    /**
     * 从数据库查询PageConfig，以数组形式返回（PS：redis的pushAll只接受数组形式才能正确存入）
     * 后面改成了用string存，因为空数组存不进redis，还是返回list（转string了）
     *
     * @param position
     * @return
     */
    private List<PageConfig> getPageConfig(String position) {
        Criteria<PageConfig, Object> criteria = Criteria.of(PageConfig.class)
                .andEqualTo(PageConfig::getFconfigType, GuideConfigType.PC_CONFIG.getCode())
                .andEqualTo(PageConfig::getFposition, Integer.valueOf(position))
                .andEqualTo(PageConfig::getFisDelete, BooleanNum.FALSE.getCode())
                .sort(PageConfig::getFsortValue);
        List<PageConfig> list = ResultUtils.getData(pageConfigApi.queryByCriteria(criteria));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList();
        }
        return list;
    }

    /**
     * 若配置中的relationId是默认值0，置为null不返回前端
     *
     * @param list
     */
    private void setNullIfZero(List<PageConfig> list) {
        for (PageConfig pageConfig : list) {
            if (pageConfig.getFrelationId() == 0) {
                pageConfig.setFrelationId(null);
            }
        }
    }

    @Override
    public Result<Set<GoodsCategoryVo>> queryCategories() {
        // 查询所有分类
        Result<List<GoodsCategory>> goodsCategoryResult = goodsCategoryApi.queryByCriteria(Criteria.of(GoodsCategory.class)
                .andEqualTo(GoodsCategory::getFisDelete, 0)
                .andEqualTo(GoodsCategory::getFisDisplay, 1));
        Ensure.that(goodsCategoryResult.isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
        List<GoodsCategory> categoryList = goodsCategoryResult.getData();
        if (CollectionUtils.isEmpty(categoryList)) {
            return Result.success(Sets.newTreeSet());
        }
        List<GoodsCategoryVo> voList = categoryList.stream().map(category -> {
            GoodsCategoryVo vo = dozerHolder.convert(category, GoodsCategoryVo.class);
            vo.setImageUrl(FileUtils.getFileUrl(category.getFcategoryUrl()));
            return vo;
        }).collect(toList());
        Map<Integer, List<GoodsCategoryVo>> levelMap = voList.stream().collect(groupingBy(GoodsCategoryVo::getFlevel));
        // 1级
        Set<GoodsCategoryVo> level1 = Sets.newTreeSet(levelMap.get(1));
        // 2级
        Set<GoodsCategoryVo> level2 = Sets.newTreeSet(levelMap.get(2));
        // 3级
        Set<GoodsCategoryVo> level3 = Sets.newTreeSet(levelMap.get(3));
        fillChildCategory(level2, level3);
        fillChildCategory(level1, level2);
        return Result.success(level1);
    }

    private void fillChildCategory(Set<GoodsCategoryVo> parentList, Set<GoodsCategoryVo> childList) {
        if (CollectionUtils.isEmpty(childList)) {
            return;
        }
        Map<Long, List<GoodsCategoryVo>> childMap = childList.stream().collect(groupingBy(GoodsCategoryVo::getFparentCategoryId));
        parentList.stream().forEach(parent -> {
            List<GoodsCategoryVo> childs = childMap.get(parent.getFcategoryId());
            if (CollectionUtils.isNotEmpty(childs)) {
                parent.setChildren(Sets.newTreeSet(childs));
            }
        });

    }
}
