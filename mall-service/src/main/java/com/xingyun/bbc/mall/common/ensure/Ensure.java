package com.xingyun.bbc.mall.common.ensure;


import com.xingyun.bbc.mall.common.ensure.extensions.*;



/**
 * 断言工具类
 *
 * @author lll
 * @version 1.0.0
 * @date 2019-08-18
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class Ensure {

    public static EnsureObjectExtension that(Object param) {
        return new EnsureObjectExtension(param);
    }
    public static EnsureStringExtension that(String param) {
        return new EnsureStringExtension(param);
    }
    public static EnsureBooleanExtension that(Boolean param) {
        return new EnsureBooleanExtension(param);
    }

}
