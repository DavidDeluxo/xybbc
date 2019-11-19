package com.xingyun.bbc.mallpc.service.impl;

import com.xingyun.bbc.core.operate.api.PageConfigApi;
import com.xingyun.bbc.core.operate.enums.BooleanNum;
import com.xingyun.bbc.core.operate.enums.GuideConfigType;
import com.xingyun.bbc.core.operate.enums.PageConfigPositionEnum;
import com.xingyun.bbc.core.operate.po.PageConfig;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.GoodsApi;
import com.xingyun.bbc.core.sku.api.GoodsBrandApi;
import com.xingyun.bbc.core.sku.po.Goods;
import com.xingyun.bbc.core.sku.po.GoodsBrand;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.utils.ResultUtils;
import com.xingyun.bbc.mallpc.model.vo.index.BannerVo;
import com.xingyun.bbc.mallpc.model.vo.index.BrandVo;
import com.xingyun.bbc.mallpc.model.vo.index.SpecialTopicVo;
import com.xingyun.bbc.mallpc.service.IndexService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant.*;

@Service
public class IndexServiceImpl implements IndexService {

    /**
     * 首页一级分类下最多展示的品牌数
     */
    private static final int BRAND_MAX = 6;

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

    @Override
    public List<SpecialTopicVo> getSpecialTopics() {
        List<PageConfig> result = (List<PageConfig>) cacheTemplate
                .range(PC_MALL_PAGECONFIG_TOPIC, PC_MALL_PAGECONFIG_TOPIC_UPDATE, () -> getPageConfig(PageConfigPositionEnum.SPECIAL_TOPIC.getKey()));
        return dozerHolder.convert(result, SpecialTopicVo.class);
    }

    @Override
    public List<BannerVo> getBanners() {
        List<PageConfig> result = (List<PageConfig>) cacheTemplate
                .range(PC_MALL_PAGECONFIG_BANNER, PC_MALL_PAGECONFIG_BANNER_UPDATE, () -> getPageConfig(PageConfigPositionEnum.BANNER.getKey()));
        return dozerHolder.convert(result, BannerVo.class);
    }

    @Override
    public List<BrandVo> getBrands(Long cateId) {
        List<GoodsBrand> result = (List<GoodsBrand>) cacheTemplate
                .get(INDEX_BRAND+cateId, INDEX_BRAND_UPDATE+cateId,60*30, () -> {
                    //查询热门品牌
                    Criteria<GoodsBrand,Object> brandCriteria = Criteria.of(GoodsBrand.class)
                            .andEqualTo(GoodsBrand::getFisDelete, 0)
                            .andEqualTo(GoodsBrand::getFisDisplay, 1)
                            .andEqualTo(GoodsBrand::getFisHot, 1);
                    List<GoodsBrand> brandList = ResultUtils.getData(goodsBrandApi.queryByCriteria(brandCriteria));
                    if (CollectionUtils.isEmpty(brandList)) {
                        return new GoodsBrand[0];
                    }
                    //查询入参一级分类id下的Goods的brandIds
                    Criteria<Goods, Object> goodsCriteria = Criteria.of(Goods.class)
                            .andEqualTo(Goods::getFcategoryId1, cateId)
                            .fields(Goods::getFbrandId);
                    List<Goods> goodsList = ResultUtils.getData(goodsApi.queryByCriteria(goodsCriteria));
                    if (CollectionUtils.isEmpty(goodsList)) {
                        return new GoodsBrand[0];
                    }
                    List<Long> brandIds = goodsList.stream().map(Goods::getFbrandId).distinct().collect(Collectors.toList());
                    //筛选出符合条件的品牌信息
                    List<GoodsBrand> goodsBrands = brandList.stream().filter(hotBrand -> brandIds.contains(hotBrand.getFbrandId())).collect(Collectors.toList());
                    int endIndex = goodsBrands.size()>BRAND_MAX?BRAND_MAX:goodsBrands.size();
                    List<GoodsBrand> rerurnBrands = goodsBrands.subList(0,endIndex);
                    return rerurnBrands.toArray(new GoodsBrand[goodsBrands.size()]);
                });
        return dozerHolder.convert(result, BrandVo.class);
    }

    @Override
    public Integer getUserCount() {
        return (Integer) cacheTemplate
                .get(INDEX_USER_COUNT, INDEX_USER_COUNT_UPDATE, DEFAULT_LOCK_EXPIRING, () -> ResultUtils.getData(userApi.count(new User())));
    }

    /**
     * 从数据库查询PageConfig，以数组形式返回（PS：redis的pushAll只接受数组形式才能正确存入）
     *
     * @param position
     * @return
     */
    private PageConfig[] getPageConfig(String position) {
        PageConfig query = new PageConfig();
        query.setFconfigType(GuideConfigType.PC_CONFIG.getCode());
        query.setFposition(Integer.valueOf(position));
        query.setFisDelete(BooleanNum.FALSE.getCode());
        List<PageConfig> list = ResultUtils.getData(pageConfigApi.queryList(query));
        if (CollectionUtils.isEmpty(list)) {
            return new PageConfig[0];
        }
        return list.toArray(new PageConfig[list.size()]);
    }
}
