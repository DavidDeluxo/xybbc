package com.xingyun.bbc.mallpc.common.utils;

import com.google.gson.GsonBuilder;
import com.xingyun.bbc.mallpc.common.constants.MallPcConstants;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-17
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class GsonUtils {

    public static String objectToJson(Object object) {
        return new GsonBuilder().setDateFormat(MallPcConstants.FULL_STANDARD_PATTERN).create().toJson(object);
    }

}
