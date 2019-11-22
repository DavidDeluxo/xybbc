package com.xingyun.bbc.mallpc.service.impl;


import com.google.common.collect.Lists;
import com.xingyun.bbc.common.redis.XyRedisManager;
import com.xingyun.bbc.core.operate.api.PageConfigApi;
import com.xingyun.bbc.core.operate.enums.PageConfigPcEnum;
import com.xingyun.bbc.core.operate.enums.PageConfigPositionEnum;
import com.xingyun.bbc.core.operate.enums.PageConfigType;
import com.xingyun.bbc.core.operate.po.PageConfig;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.model.vo.pageconfig.PageConfigPcVo;
import com.xingyun.bbc.mallpc.service.PageConfigPcService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class PageConfigPcServiceImpl implements PageConfigPcService {

    private static Set<Integer> categoryLevel = new HashSet<>();

    static {
        categoryLevel.add(1);
        categoryLevel.add(2);
        categoryLevel.add(3);
    }

    @Resource
    private PageConfigApi pageConfigApi;

    @Resource
    private XyRedisManager xyRedisManager;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;







    @Override
    public List<PageConfigPcVo> navigation() {
        List<PageConfig> pageConfig = getPageConfig(Integer.valueOf(PageConfigPositionEnum.NAVIGATION.getKey()));
        if (CollectionUtils.isEmpty(pageConfig)) {
            return Lists.newArrayList();
        }

        ArrayList<PageConfigPcVo> target = new ArrayList(pageConfig.size());
        for (PageConfig each : pageConfig) {
            PageConfigPcVo pageConfigPcVo = new PageConfigPcVo();
            pageConfigPcVo.setFconfigName(each.getFconfigName());
            pageConfigPcVo.setFsortValue(each.getFsortValue());
            pageConfigPcVo.setFtype(each.getFtype());
            pageConfigPcVo.setFrelationId(each.getFrelationId());
            if (each.getFtype().compareTo(Integer.valueOf(PageConfigPcEnum.GOODS_CATEGORY.getKey())) == 0) {
                pageConfigPcVo.setFcategoryLevel(each.getFcategoryLevel());
            }
            target.add(pageConfigPcVo);
        }
        target.sort(Comparator.comparing(PageConfigPcVo::getFsortValue));
        return target;
    }

    private List<PageConfig> getPageConfig(Integer position) {
        Result<List<PageConfig>> listResult = pageConfigApi.queryByCriteria(Criteria.of(PageConfig.class)
                .andEqualTo(PageConfig::getFisDelete, 0)
                .andEqualTo(PageConfig::getFconfigType, PageConfigType.PC_CONFIG.getCode())
                .andEqualTo(PageConfig::getFposition, position)
                .sort(PageConfig::getFsortValue));
        Ensure.that(listResult).isSuccess(new MallPcExceptionCode(listResult.getCode(), listResult.getMsg()));
        return listResult.getData();
    }

}
