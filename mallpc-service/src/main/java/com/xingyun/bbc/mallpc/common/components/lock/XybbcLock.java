package com.xingyun.bbc.mallpc.common.components.lock;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 分布式锁
 *
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-17
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public interface XybbcLock {

    /**
     * 加锁
     *
     * @param key
     * @param value
     * @param expiring 单位秒
     * @return
     */
    boolean tryLock(String key, String value, long expiring);

    /**
     * 解锁
     *
     * @param key
     * @param value
     * @return
     */
    boolean releaseLock(String key, String value);

    /**
     * 加锁尝试几次
     *
     * @param key      锁key
     * @param value
     * @param times    尝试次数
     * @param expiring 单位秒
     * @return
     */
    boolean tryLockTimes(String key, String value, int times, long expiring);

    /**
     * 尝试获取分布式锁，执行业务逻辑后自动释放锁
     * <p>
     * this.tryLock(key, 10, lock -> {
     * if (lock) {
     * 处理业务逻辑......
     * } else {
     * throw Exception
     * }
     * });
     *
     * @param key
     * @param expiring
     * @param consumer
     */
    void tryLock(String key, long expiring, Consumer<Boolean> consumer);

    /**
     * 尝试获取分布式锁成功后，执行业务逻辑后自动释放锁
     * <p>
     * this.tryLock(key, 10, lock -> {
     * 处理业务逻辑......
     * });
     *
     * @param key
     * @param expiring
     * @param supplier
     */
    <T> T tryLock(String key, long expiring, Supplier<T> supplier);

}
