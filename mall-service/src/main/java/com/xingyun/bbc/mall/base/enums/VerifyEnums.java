package com.xingyun.bbc.mall.base.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author ZSY
 * @Title:
 * @Description:
 * @date 2019-10-14
 */
public interface VerifyEnums {
    @Getter
    enum platform{
        TAOBAO(1,"淘宝"),
        TMALL(2,"天猫"),
        JDCOM(3,"京东"),
        SUNING(4,"苏宁"),
        BEIBEI(5,"贝贝"),
        ;
        private Integer code;
        private String msg;

        platform(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public static platform findByCode(Integer code) {
            return Arrays.stream(values()).filter(type -> Objects.equals(code, type.getCode())).findFirst().orElse(null);
        }

        public static platform findByMsg(String msg) {
            return Arrays.stream(values()).filter(type -> Objects.equals(msg, type.getMsg())).findFirst().orElse(null);
        }

    }
}
