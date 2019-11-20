package com.xingyun.bbc.mallpc.common.utils;

/**
 * @author pengaoluo
 * @version 1.0.0
 */
public class BizHelper {

    public static boolean isLogicNull(Number num) {
        return num == null || num.intValue() == 0;
    }

    public static boolean isNotLogicNull(Number num) {
        return !isLogicNull(num);
    }

}
