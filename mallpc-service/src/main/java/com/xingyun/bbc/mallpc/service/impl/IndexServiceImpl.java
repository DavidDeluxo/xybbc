package com.xingyun.bbc.mallpc.service.impl;

import com.xingyun.bbc.core.operate.api.PageConfigApi;
import com.xingyun.bbc.core.operate.enums.BooleanNum;
import com.xingyun.bbc.core.operate.enums.GuideConfigType;
import com.xingyun.bbc.core.operate.enums.PageConfigPositionEnum;
import com.xingyun.bbc.core.operate.po.PageConfig;
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

import static com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant.*;

@Service
public class IndexServiceImpl implements IndexService {

    @Resource
    private UserApi userApi;
    @Resource
    private PageConfigApi pageConfigApi;
    @Resource
    private CacheTemplate cacheTemplate;
    @Resource
    private DozerHolder dozerHolder;

    @Override
    public List<SpecialTopicVo> getSpecialTopics() {
        List<PageConfig> result = (List<PageConfig>) cacheTemplate
                .range(PC_MALL_PAGECONFIG_BANNER, PC_MALL_PAGECONFIG_BANNER_UPDATE, () -> getPageConfig(PageConfigPositionEnum.SPECIAL_TOPIC.getKey()));
        return dozerHolder.convert(result, SpecialTopicVo.class);
    }

    @Override
    public List<BannerVo> getBanners() {
        List<PageConfig> result = (List<PageConfig>) cacheTemplate
                .range(PC_MALL_PAGECONFIG_BANNER, PC_MALL_PAGECONFIG_BANNER_UPDATE, () -> getPageConfig(PageConfigPositionEnum.BANNER.getKey()));
        return dozerHolder.convert(result, BannerVo.class);
    }

    @Override
    public List<BrandVo> getBrands() {
//        return (List<BrandVo>) cacheTemplate.get(PC_MALL_PAGECONFIG_BANNER,PC_MALL_PAGECONFIG_BANNER_UPDATE,10l,()->{
//            PageConfig query = new PageConfig();
//            query.setFconfigType(GuideConfigType.PC_CONFIG.getCode());
//            query.setFposition(Integer.valueOf(PageConfigPositionEnum.BANNER.getKey()));
//            query.setFisDelete(BooleanNum.FALSE.getCode());
//            List<PageConfig> result = ResultUtils.getData(pageConfigApi.queryList(query));
//            return dozerHolder.convert(result,BannerVo.class);
//        });
//        //添加热门品牌
//        Result<List<GoodsBrand>> hotBrandListResult = goodsBrandApi.queryByCriteria(Criteria.of(GoodsBrand.class)
//                .andEqualTo(GoodsBrand::getFisDelete, 0)
//                .andEqualTo(GoodsBrand::getFisDisplay, 1)
//                .andEqualTo(GoodsBrand::getFisHot, 1));
//        if (!hotBrandListResult.isSuccess()) {
//            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
//        }
        return null;
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
