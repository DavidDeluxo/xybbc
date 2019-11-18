package com.xingyun.bbc.mallpc.common.ensure;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.ensure.extensions.*;

import java.util.Collection;

/**
 * 断言工具类
 *
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-18
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class Ensure {

    public static EnsureObjectExtension that(Object param) {
        return new EnsureObjectExtension(param);
    }

    public static EnsureArrayExtension that(Object[] param) {
        return new EnsureArrayExtension(param);
    }

    public static EnsureCollectionExtension that(Collection param) {
        return new EnsureCollectionExtension(param);
    }

    public static EnsureBooleanExtension that(Boolean param) {
        return new EnsureBooleanExtension(param);
    }

    public static EnsureNumberExtension that(Number param) {
        return new EnsureNumberExtension(param);
    }

    public static EnsureEnumExtension that(Enum param) {
        return new EnsureEnumExtension(param);
    }

    public static EnsureStringExtension that(String param) {
        return new EnsureStringExtension(param);
    }

    public static EnsureResultExtension that(Result param) {
        return new EnsureResultExtension(param);
    }

}
