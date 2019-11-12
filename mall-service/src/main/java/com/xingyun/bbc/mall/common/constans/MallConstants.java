package com.xingyun.bbc.mall.common.constans;

import java.math.BigDecimal;

public interface MallConstants {

    //是否被删除
    Integer ISDELETE_NO = 0;
    Integer ISDELETE_YES = 1;

    BigDecimal ONE_HUNDRED = new BigDecimal("100");
    BigDecimal ONE_THOUSAND = new BigDecimal("1000");
    BigDecimal TEN_THOUSAND = new BigDecimal("10000");

    public static final String MALL_RECEIVE_COUPON = "mall_receive_coupon_lock";
}
