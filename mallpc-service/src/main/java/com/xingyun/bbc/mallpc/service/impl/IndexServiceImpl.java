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
import com.xingyun.bbc.mallpc.common.utils.FileUtils;
import com.xingyun.bbc.mallpc.common.utils.ResultUtils;
import com.xingyun.bbc.mallpc.model.vo.index.BannerVo;
import com.xingyun.bbc.mallpc.model.vo.index.BrandVo;
import com.xingyun.bbc.mallpc.model.vo.index.SpecialTopicVo;
import com.xingyun.bbc.mallpc.service.IndexService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant.*;

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
                .get(INDEX_USER_COUNT, INDEX_USER_COUNT_UPDATE, USER_COUNT_EXPIRE, () -> ResultUtils.getData(userApi.count(new User())));
    }

    @Override
    public List<BrandVo> getBrands(Long cateId) {
        List<GoodsBrand> result = (List<GoodsBrand>) cacheTemplate
                .get(INDEX_BRAND + cateId, INDEX_BRAND_UPDATE + cateId, BRAND_EXPIRE, () -> {
                    //查询热门品牌
                    Criteria<GoodsBrand, Object> brandCriteria = Criteria.of(GoodsBrand.class)
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
                    int endIndex = goodsBrands.size() > BRAND_MAX ? BRAND_MAX : goodsBrands.size();
                    List<GoodsBrand> returnBrands = goodsBrands.subList(0, endIndex);
                    return returnBrands;
                });
        List<BrandVo> vos = dozerHolder.convert(result, BrandVo.class);
        vos.forEach(vo -> {
            vo.setFbrandLogo(FileUtils.getFileUrl(vo.getFbrandLogo()));
            vo.setFbrandPoster(FileUtils.getFileUrl(vo.getFbrandPoster()));
        });
        return vos;
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
     * @param list
     */
    private void setNullIfZero(List<PageConfig> list){
        for (PageConfig pageConfig : list) {
            if (pageConfig.getFrelationId() == 0) {
                pageConfig.setFrelationId(null);
            }
        }
    }
}
