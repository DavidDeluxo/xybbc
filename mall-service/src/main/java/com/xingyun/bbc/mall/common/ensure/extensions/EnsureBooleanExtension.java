package com.xingyun.bbc.mall.common.ensure.extensions;

import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import org.apache.commons.lang3.BooleanUtils;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-18
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class EnsureBooleanExtension {

    private Boolean param;

    public EnsureBooleanExtension(Boolean param) {
        this.param = param;
    }

    public EnsureBooleanExtension isFalse(MallExceptionCode errorCode) {
        if (BooleanUtils.toBoolean(this.param.booleanValue())) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureBooleanExtension isTrue(MallExceptionCode errorCode) {
        if (!BooleanUtils.toBoolean(this.param.booleanValue())) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureBooleanExtension isNotNull(MallExceptionCode errorCode) {
        if (this.param == null) {
            throw new BizException(errorCode);
        }
        return this;
    }
}
