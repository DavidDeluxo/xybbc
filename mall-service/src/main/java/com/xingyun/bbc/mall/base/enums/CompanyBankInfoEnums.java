package com.xingyun.bbc.mall.base.enums;


import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author 
 * @Description: 线下充值公司信息
 * @createTime: 2019-08-23 16:30
 */
public interface CompanyBankInfoEnums {

    @Getter
    enum offlinePay { 
    	COMPANYNAME(1, "深圳市天行云供应链有限公司"),
        COMPANYBANKCARD(2, "44250100008800001558"),
        COMPANYBANKNAME(3, "中国建设银行股份有限公司深圳石厦支行"),
        ;
        private Integer type;
        private String desc;

        offlinePay(Integer type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        public static offlinePay findByCode(Integer code) {
            return Arrays.stream(values()).filter(type -> Objects.equals(code, type.getType())).findFirst().orElse(null);
        }

    }


    Integer getCode();

    String getDesc();

}
