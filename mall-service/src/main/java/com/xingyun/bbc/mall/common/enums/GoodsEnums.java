package com.xingyun.bbc.mall.common.enums;

import lombok.Getter;

/**
 * @author yl
 * @version 1.0.0
 * @date 2019-08-20
 * @copyright
 */
public interface GoodsEnums {

    @Getter
    enum GoodsImageType {
        MAIN_PICTURE(0, "商品图片"),
        THUMBNAIL(1, "详情图片");

        private Integer code;
        private String desc;

        GoodsImageType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

    }



}
