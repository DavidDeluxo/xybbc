package com.xingyun.bbc.mallpc.service.impl;

import com.xingyun.bbc.common.redis.XyRedisManager;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.mallpc.common.components.lock.XybbcLock;
import com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant;
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

@Service
public class IndexServiceImpl implements IndexService {

    @Resource
    private UserApi userApi;
    @Autowired
    private XyRedisManager xyRedisManager;
    @Resource
    private XybbcLock xybbcLock;

    @Override
    public List<SpecialTopicVo> getSpecialTopics() {
        return null;
    }

    @Override
    public List<BannerVo> getBanners() {
        return null;
    }

    @Override
    public List<BrandVo> getBrands() {
        return null;
    }

    @Override
    public Integer getUserCount() {
        //缓存查询用户数
        Integer userCount = (Integer) xyRedisManager.get(MallPcRedisConstant.INDEX_USER_COUNT);
        if (userCount != null && userCount > 0) {
            return userCount;
        }
        boolean getLock = false;
        try {
            //分布式锁
            Ensure.that(getLock = xybbcLock.tryLock(MallPcRedisConstant.INDEX_USER_COUNT_UPDATE, MallPcRedisConstant.DEFAULT_LOCK_VALUE, MallPcRedisConstant.DEFAULT_LOCK_EXPIRING)).isTrue(MallPcExceptionCode.SYSTEM_BUSY_ERROR);
            //获取锁后再查询一次缓存是否有值，有直接返回
            userCount = (Integer) xyRedisManager.get(MallPcRedisConstant.INDEX_USER_COUNT);
            if (userCount != null && userCount > 0) {
                return userCount;
            }
            //没有值则查询数据库，更新到缓存，并返回
            userCount = ResultUtils.getData(userApi.count(null));
            xyRedisManager.set(MallPcRedisConstant.INDEX_USER_COUNT,userCount,MallPcRedisConstant.DEFAULT_LOCK_EXPIRING);
            return userCount;
        } finally {
            if (getLock) {
                xybbcLock.releaseLock(MallPcRedisConstant.INDEX_USER_COUNT_UPDATE, MallPcRedisConstant.DEFAULT_LOCK_VALUE);
            }
        }
    }
}
