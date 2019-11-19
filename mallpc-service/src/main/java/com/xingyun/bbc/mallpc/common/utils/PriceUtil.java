package com.xingyun.bbc.mallpc.common.utils;

import java.math.BigDecimal;

/**
 * @author hekaijin
 * @Description:
 * @createTime: 2019-09-11 14:51
 */
public class PriceUtil {

	public static final BigDecimal ZERO = new BigDecimal(0.00);
	
    public static final BigDecimal PRICE_UNIT = new BigDecimal(100);

    public static BigDecimal toYuan(int penny){
        return toYuan(new BigDecimal(penny));
    }

    public static BigDecimal toYuan(double penny){
        return toYuan(new BigDecimal(penny));
    }

    public static BigDecimal toYuan(long penny){
        return toYuan(new BigDecimal(penny));
    }

    public static BigDecimal toYuan(String penny){
        return toYuan(new BigDecimal(penny));
    }

    public static BigDecimal toYuan(Object penny) {
        return toYuan(new BigDecimal(String.valueOf(penny)));
    }


    /**
     * 将分转换为元
     * @param penny
     * @return
     */
    public static BigDecimal toYuan(BigDecimal penny) {
        return penny.divide(PRICE_UNIT).setScale(2, BigDecimal.ROUND_HALF_UP);
    }


    public static BigDecimal toPenny(int yuan) {
        return toPenny(new BigDecimal(yuan));
    }

    public static BigDecimal toPenny(double yuan) {
        return toPenny(new BigDecimal(yuan));
    }

    public static BigDecimal toPenny(long yuan) {
        return toPenny(new BigDecimal(yuan));
    }

    public static BigDecimal toPenny(String yuan) {
        return toPenny(new BigDecimal(yuan));
    }

    public static BigDecimal toPenny(Object yuan) {
        return toPenny(new BigDecimal(String.valueOf(yuan)));
    }

    /**
     * 将元转换为分
     * @param yuan
     * @return
     */
    public static BigDecimal toPenny(BigDecimal yuan) {
        return yuan.multiply(PRICE_UNIT).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
