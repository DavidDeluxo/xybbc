package com.xingyun.bbc.mall.common.lock.impl;

import com.xingyun.bbc.mall.base.utils.RandomUtils;
import com.xingyun.bbc.mall.common.ensure.Ensure;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.common.lock.Execute;
import com.xingyun.bbc.mall.common.lock.XybbcLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;
import redis.clients.util.SafeEncoder;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Redis分布式锁
 *
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-17
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Service
@Slf4j
public class RedisLock implements XybbcLock {

    //通过Lua脚本删除key,确保原子操作
    private static final String DEL_LUA_SCRIPT = "if redis.call('GET', KEYS[1]) == ARGV[1] then return redis.call('DEL', KEYS[1]) else return 0 end";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean tryLock(String key, String value, long expiring) {
        Boolean result = redisTemplate.execute((RedisCallback<Boolean>) redisConnection ->
                redisConnection.set(SafeEncoder.encode(key), SafeEncoder.encode(value), Expiration.seconds(expiring),
                        RedisStringCommands.SetOption.SET_IF_ABSENT));
        return result.booleanValue();
    }

    @Override
    public boolean releaseLock(String key, String value) {
        Boolean result = redisTemplate.execute((RedisCallback<Boolean>) redisConnection ->
                redisConnection.eval(DEL_LUA_SCRIPT.getBytes(), ReturnType.BOOLEAN, 1,
                        SafeEncoder.encode(key), SafeEncoder.encode(value)));
        return result.booleanValue();
    }

    @Override
    public boolean tryLockTimes(String key, String value, int times, long expiring) {
        times = times <= 0 ? 1 : times;
        times = times > 10 ? 10 : times;
        for (int i = 0; i < times; i++) {
            boolean getLock = this.tryLock(key, value, expiring);
            if (getLock) {
                return true;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(RandomUtils.randomInt(100) + 1);
            } catch (InterruptedException e) {
                log.error("tryLockTimes sleep error...", e);
            }
        }
        return false;
    }

    @Override
    public void tryLock(String key, long expiring, Consumer<Boolean> consumer) {
        String value = RandomUtils.getUUID();
        boolean success = false;
        try {
            success = this.tryLock(key, value, expiring);
            consumer.accept(success);
        } finally {
            if (success) {
                this.releaseLock(key, value);
            }
        }
    }

    /**
     * 重试20次，最多锁10秒
     *
     * @param key
     * @param execute
     */
    @Override
    public void tryLock(String key, Execute execute) {
        String value = RandomUtils.getUUID();
        boolean success = false;
        try {
            for (int i = 0; i < 20; i++) {
                success = this.tryLock(key, value, 10);
                if (success) {
                    execute.execute();
                    break;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(RandomUtils.randomInt(100) + 1);
                } catch (InterruptedException e) {
                    log.error("tryLockTimes sleep error...", e);
                }
            }
        } finally {
            if (success) {
                this.releaseLock(key, value);
            }
        }
    }

    @Override
    public <T> T tryLock(String key, long expiring, Supplier<T> supplier) {
        String value = RandomUtils.getUUID();
        boolean success = false;
        try {
            success = this.tryLock(key, value, expiring);
            Ensure.that(success).isTrue(MallExceptionCode.SYSTEM_BUSY_ERROR);
            return supplier.get();
        } finally {
            if (success) {
                this.releaseLock(key, value);
            }
        }
    }

}
