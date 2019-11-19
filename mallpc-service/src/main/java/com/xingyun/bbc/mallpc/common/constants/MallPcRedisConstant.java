package com.xingyun.bbc.mallpc.common.constants;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-21
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public interface MallPcRedisConstant {

    /**
     * 默认分布式锁过期时间
     */
    long DEFAULT_LOCK_EXPIRING = 3L;

    String KEY_PREFIX = "mall_pc:";

    /**
     * 默认分布式锁value
     */
    String DEFAULT_LOCK_VALUE = "";

    /**
     * 首页用户数
     */
    String INDEX_USER_COUNT = KEY_PREFIX+"index_user_count";
    /**
     * 首页用户数缓存更新分布式锁前缀
     */
    String INDEX_USER_COUNT_UPDATE = INDEX_USER_COUNT+"_update";

    /**
     * 首页配置 Banner key
     */
    String PC_MALL_PAGECONFIG_BANNER = "pc_mall_banner";
    /**
     * pc首页banner更新时redis分布式锁前缀
     */
    String PC_MALL_PAGECONFIG_BANNER_UPDATE = "pc_mall_banner_update";

    /**
     * 首页配置 专题位 key
     */
    String PC_MALL_PAGECONFIG_TOPIC = "pc_mall_topic";
    /**
     * pc首页专题位更新时redis分布式锁前缀
     */
    String PC_MALL_PAGECONFIG_TOPIC_UPDATE = "pc_mall_topic_update";


    String VERIFY_CODE_PREFIX = "SMS_";
}
