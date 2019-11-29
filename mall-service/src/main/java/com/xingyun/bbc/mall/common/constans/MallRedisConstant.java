package com.xingyun.bbc.mall.common.constans;

/**
 * @author hekaijin
 * @date 2019/9/21 13:00
 * @Description
 */
public interface MallRedisConstant {

    String KEY_PREFIX = "xybbc_mall_service:";

    String ADD_USER_WITHDRAW_LOCK = KEY_PREFIX + "add_user_withdraw_lock";

    String GOODS_CATEGORIES_CACHE = KEY_PREFIX + "goods_categories_cache";

    /**
     * 首页用户数
     */
    String USER_COUNT = "user_count";
    /**
     * 首页用户数缓存更新分布式锁前缀
     */
    String USER_COUNT_LOCK = "user_count_lock";
}
