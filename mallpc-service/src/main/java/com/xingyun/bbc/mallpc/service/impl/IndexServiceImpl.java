package com.xingyun.bbc.mallpc.service.impl;

import com.xingyun.bbc.common.redis.XyRedisManager;
import com.xingyun.bbc.core.operate.api.PageConfigApi;
import com.xingyun.bbc.core.operate.enums.BooleanNum;
import com.xingyun.bbc.core.operate.enums.GuideConfigType;
import com.xingyun.bbc.core.operate.enums.PageConfigPositionEnum;
import com.xingyun.bbc.core.operate.po.PageConfig;
import com.xingyun.bbc.core.rpc.Api;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.components.lock.XybbcLock;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.ResultUtils;
import com.xingyun.bbc.mallpc.model.vo.index.BannerVo;
import com.xingyun.bbc.mallpc.model.vo.index.BrandVo;
import com.xingyun.bbc.mallpc.model.vo.index.SpecialTopicVo;
import com.xingyun.bbc.mallpc.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
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
        return null;
    }

    @Override
    public List<BannerVo> getBanners() {
        return (List<BannerVo>) cacheTemplate.execute(PC_MALL_PAGECONFIG_BANNER,PC_MALL_PAGECONFIG_BANNER_UPDATE,10l,()->{
            PageConfig query = new PageConfig();
            query.setFconfigType(GuideConfigType.PC_CONFIG.getCode());
            query.setFposition(Integer.valueOf(PageConfigPositionEnum.BANNER.getKey()));
            query.setFisDelete(BooleanNum.FALSE.getCode());
            List<PageConfig> result = ResultUtils.getData(pageConfigApi.queryList(query));
            return dozerHolder.convert(result,BannerVo.class);
        });
    }

    @Override
    public List<BrandVo> getBrands() {
        return null;
    }

    @Override
    public Integer getUserCount() {
        return (Integer) cacheTemplate.execute(INDEX_USER_COUNT,INDEX_USER_COUNT_UPDATE,DEFAULT_LOCK_EXPIRING,()->{
            User user = new User();
            return ResultUtils.getData(userApi.count(user));
        });
    }
}
