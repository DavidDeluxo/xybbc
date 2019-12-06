package com.xingyun.bbc.mallpc.service.impl;

import com.xingyun.bbc.mallpc.common.components.RedisHolder;
import com.xingyun.bbc.mallpc.common.components.lock.XybbcLock;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.RandomUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

import static com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant.DEFAULT_LOCK_EXPIRING;
import static com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant.DEFAULT_LOCK_VALUE;

@Slf4j
@Component
public class CacheTemplate {

    /**
     * 分布式锁默认重试次数
     */
    private static final int TRY_TIMES = 10;

    @Autowired
    private RedisHolder redisHolder;
    @Resource
    private XybbcLock xybbcLock;

    /**
     * @param key
     * @param updateKey
     * @param expire 过期时间（秒），不传默认是不过期
     * @param cacheCallBack
     * @return
     */
    public Object get(String key, String updateKey, Long expire, CacheCallBack cacheCallBack) {
        //若缓存存在，查询并返回
        if (redisHolder.exists(key)) {
            return redisHolder.getObject(key);
        }
        boolean getLock = false;
        String value = RandomUtils.getUUID();
        try {
            Ensure.that(getLock = xybbcLock.tryLockTimes(updateKey, value, TRY_TIMES, DEFAULT_LOCK_EXPIRING)).isTrue(MallPcExceptionCode.SYSTEM_BUSY_ERROR);
            //获取锁后再查询一次缓存是否有值，有直接返回
            if (redisHolder.exists(key)) {
                return redisHolder.getObject(key);
            }
            //没有值则查询数据库，更新到缓存，并返回
            Object result = cacheCallBack.callBack();
            boolean isSuccess = redisHolder.set(key, result, expire);
            log.info("缓存{}更新结果：{}", key, isSuccess);
            return result;
        } finally {
            if (getLock) {
                xybbcLock.releaseLock(updateKey, value);
            }
        }
    }

    /**
     * 从缓存中查询key对应的数据，若存在直接返回
     * 否则尝试获取分布式锁，双重校验缓存中是否有对应数据，没有从cacheCallBack读取放入缓存并返回
     *
     * @param key
     * @param updateKey
     * @param cacheCallBack
     * @return
     */
    public Object range(String key, String updateKey, CacheListCallBack cacheCallBack) {
        //若缓存存在，查询并返回
        if (redisHolder.exists(key)) {
            Object value = redisHolder.range(key, 0, -1);
            return value;
        }
        boolean getLock = false;
        String value = RandomUtils.getUUID();
        try {
            //分布式锁
//            getLock = xybbcLock.tryLock(updateKey, DEFAULT_LOCK_VALUE, DEFAULT_LOCK_EXPIRING);
//            if(!getLock){
//                log.info("分布式锁获取失败，直接从数据库查询");
//                Object[] result = cacheCallBack.callBack();
//                return Arrays.asList(result);
//            }
            Ensure.that(getLock = xybbcLock.tryLockTimes(updateKey, value, TRY_TIMES, DEFAULT_LOCK_EXPIRING)).isTrue(MallPcExceptionCode.SYSTEM_BUSY_ERROR);

            //获取锁后再查询一次缓存是否有值，有直接返回
            if (redisHolder.exists(key)) {
                return redisHolder.range(key, 0, -1);
            }
            //没有值则查询数据库，更新到缓存，并返回
            Object[] result = cacheCallBack.callBack();
            boolean isTrue = redisHolder.pushAll(key, result);
            log.info("缓存更新结果：{}", isTrue);
            return Arrays.asList(result);
        } finally {
            if (getLock) {
                xybbcLock.releaseLock(updateKey, value);
            }
        }
    }
}
