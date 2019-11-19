package com.xingyun.bbc.mallpc.service.impl;

import com.xingyun.bbc.mallpc.common.components.RedisHolder;
import com.xingyun.bbc.mallpc.common.components.lock.XybbcLock;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
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

    @Autowired
    private RedisHolder redisHolder;
    @Resource
    private XybbcLock xybbcLock;

    /**
     * @param key
     * @param updateKey
     * @param expire        过期时间若不传，则缓存不会过期
     * @param cacheCallBack
     * @return
     */
    public Object get(String key, String updateKey, long expire, CacheCallBack cacheCallBack) {
        //若缓存存在，查询并返回
        if (redisHolder.exists(key)) {
            Object value = redisHolder.get(key);
            return value;
        }
        boolean getLock = false;
        try {
            //分布式锁
            Ensure.that(getLock = xybbcLock.tryLock(updateKey, DEFAULT_LOCK_VALUE, DEFAULT_LOCK_EXPIRING)).isTrue(MallPcExceptionCode.SYSTEM_BUSY_ERROR);
            //获取锁后再查询一次缓存是否有值，有直接返回
            if (redisHolder.exists(key)) {
                return redisHolder.get(key);
            }
            //没有值则查询数据库，更新到缓存，并返回
            Object result = cacheCallBack.callBack();
            redisHolder.put(key, result, expire);
            return result;
        } finally {
            if (getLock) {
                log.info("释放释放释放释放释放释放释放释放释放释放释放释放释放释放");
                xybbcLock.releaseLock(updateKey, DEFAULT_LOCK_VALUE);
            }
        }
    }

    /**
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
        try {
            //分布式锁
//            getLock = xybbcLock.tryLock(updateKey, DEFAULT_LOCK_VALUE, DEFAULT_LOCK_EXPIRING);
//            if(!getLock){
//                Object[] result = cacheCallBack.callBack();
//                return Arrays.asList(result);
//            }
            Ensure.that(getLock = xybbcLock.tryLockTimes(updateKey, DEFAULT_LOCK_VALUE, 5,DEFAULT_LOCK_EXPIRING)).isTrue(MallPcExceptionCode.SYSTEM_BUSY_ERROR);

            //获取锁后再查询一次缓存是否有值，有直接返回
            if (redisHolder.exists(key)) {
                return redisHolder.range(key,0, -1);
            }
            //没有值则查询数据库，更新到缓存，并返回
            Object[] result = cacheCallBack.callBack();
            redisHolder.pushAll(key, result);
            return Arrays.asList(result);
        } finally {
            if (getLock) {
                xybbcLock.releaseLock(updateKey, DEFAULT_LOCK_VALUE);
            }
        }
    }
}
