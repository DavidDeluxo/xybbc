package com.xingyun.bbc.mallpc.common.enums.search;

/**
 * @author chenxiang
 * @version 1.0.0
 * @date 2019/8/27
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public enum DeclareSearchTypeEnum {
    OrderId("订单号", 1),
    PaymentId("支付单号", 2);
    private String name;
    private Integer value;

    DeclareSearchTypeEnum(String name, Integer index) {
        this.name = name;
        this.value = index;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }


}