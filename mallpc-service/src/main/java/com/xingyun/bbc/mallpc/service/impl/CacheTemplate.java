package com.xingyun.bbc.mallpc.service.impl;

import com.xingyun.bbc.common.redis.XyRedisManager;
import com.xingyun.bbc.mallpc.common.components.lock.XybbcLock;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant.*;
import static com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant.DEFAULT_LOCK_VALUE;

@Slf4j
@Component
public class CacheTemplate {

    @Autowired
    private XyRedisManager xyRedisManager;
    @Resource
    private XybbcLock xybbcLock;

    /**
     *
     * @param key
     * @param updateKey
     * @param expire 过期时间若不传，则缓存不会过期
     * @param cacheCallBack
     * @return
     */
    public Object execute(String key, String updateKey, long expire,CacheCallBack cacheCallBack){
        //若缓存存在，查询并返回
        if(xyRedisManager.exists(key)){
            return xyRedisManager.get(key);
        }
        log.info("________________________________");
        boolean getLock = false;
        try {
            //分布式锁
            Ensure.that(getLock = xybbcLock.tryLock(updateKey, DEFAULT_LOCK_VALUE, DEFAULT_LOCK_EXPIRING)).isTrue(MallPcExceptionCode.SYSTEM_BUSY_ERROR);
            //获取锁后再查询一次缓存是否有值，有直接返回
            if(xyRedisManager.exists(key)){
                return xyRedisManager.get(key);
            }
            //没有值则查询数据库，更新到缓存，并返回
            Object result =  cacheCallBack.callBack();
            xyRedisManager.set(key,result, expire);
            return result;
        } finally {
            if (getLock) {
                log.info("释放释放释放释放释放释放释放释放释放释放释放释放释放释放");
                xybbcLock.releaseLock(updateKey, DEFAULT_LOCK_VALUE);
            }
        }
    }

    /**
     *
     * @param key
     * @param updateKey
     * @param cacheCallBack
     * @return
     */
    public Object execute(String key, String updateKey,CacheCallBack cacheCallBack){
        //若缓存存在，查询并返回
        if(xyRedisManager.exists(key)){
            return xyRedisManager.get(key);
        }
        boolean getLock = false;
        try {
            //分布式锁
            Ensure.that(getLock = xybbcLock.tryLock(updateKey, DEFAULT_LOCK_VALUE, DEFAULT_LOCK_EXPIRING)).isTrue(MallPcExceptionCode.SYSTEM_BUSY_ERROR);
            //获取锁后再查询一次缓存是否有值，有直接返回
            if(xyRedisManager.exists(key)){
                return xyRedisManager.get(key);
            }
            //没有值则查询数据库，更新到缓存，并返回
            Object result =  cacheCallBack.callBack();
            xyRedisManager.set(key,result);
            return result;
        } finally {
            if (getLock) {
                xybbcLock.releaseLock(updateKey, DEFAULT_LOCK_VALUE);
            }
        }
    }
}
