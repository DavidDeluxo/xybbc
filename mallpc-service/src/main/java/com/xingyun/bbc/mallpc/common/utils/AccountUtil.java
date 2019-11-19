package com.xingyun.bbc.mallpc.common.utils;

import java.math.BigDecimal;

/**
 * @author pengaoluo
 * @version 1.0.0
 * @date 2019/8/26
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class AccountUtil {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);


    private static final BigDecimal TEN_THOUSAND = new BigDecimal(10000);


    private AccountUtil() {
    }

    /**
     * 以分为单位的数据库金额转换为以元为单位的实际金额
     * Long转BigDecimal，除以100
     *
     * @return
     */
    public static BigDecimal divideOneHundred(Long amount) {
        if (amount == null) {
            return null;
        }
        return BigDecimal.valueOf(amount).divide(ONE_HUNDRED, 2, BigDecimal.ROUND_HALF_UP);
    }

    public static String divideOneHundredAndGetString(Long amount) {
        if (amount == null) {
            return null;
        }
        return divideOneHundred(amount).toString();
    }

    /**
     * Long转BigDecimal，除以10000
     *
     * @param amount
     * @return
     */
    public static BigDecimal divideTenThousand(Long amount) {
        if (amount == null) {
            return null;
        }
        return BigDecimal.valueOf(amount).divide(TEN_THOUSAND);
    }

    /**
     * BigDecimal转Long，乘以100
     *
     * @param amount
     * @return
     */
    public static Long multiplyOneHundred(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.multiply(ONE_HUNDRED).longValue();
    }

    /**
     * BigDecimal转Long，乘以10000
     *
     * @param amount
     * @return
     */
    public static Long multiplyTenThousand(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.multiply(TEN_THOUSAND).longValue();
    }

    /**
     * 除法 四舍五入 保留小数点2位
     *
     * @return
     */
    public static BigDecimal divideHalfUp(BigDecimal value1, BigDecimal value2) {
        return value1.divide(value2, 2, BigDecimal.ROUND_HALF_UP);
    }

}
