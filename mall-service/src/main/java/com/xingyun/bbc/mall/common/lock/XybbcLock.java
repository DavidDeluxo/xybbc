package com.xingyun.bbc.mall.common.lock;

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

}
