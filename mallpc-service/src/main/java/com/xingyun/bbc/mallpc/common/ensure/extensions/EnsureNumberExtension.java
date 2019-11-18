package com.xingyun.bbc.mallpc.common.ensure.extensions;


import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;

import java.util.Objects;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-18
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class EnsureNumberExtension {

    private Number param;

    public EnsureNumberExtension(Number param) {
        this.param = param;
    }

    public EnsureNumberExtension isGt(Number param, MallPcExceptionCode errorCode) {
        if (this.param.doubleValue() <= param.doubleValue()) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureNumberExtension isNotGt(Number param, MallPcExceptionCode errorCode) {
        if (this.param.doubleValue() > param.doubleValue()) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureNumberExtension isLt(Number param, MallPcExceptionCode errorCode) {
        if (this.param.doubleValue() >= param.doubleValue()) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureNumberExtension isNotLt(Number param, MallPcExceptionCode errorCode) {
        if (this.param.doubleValue() < param.doubleValue()) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureNumberExtension isEqual(Number param, MallPcExceptionCode errorCode) {
        if (!Objects.equals(this.param, param)) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureNumberExtension isNotEqual(Number param, MallPcExceptionCode errorCode) {
        if (Objects.equals(this.param, param)) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureNumberExtension isNotNull(MallPcExceptionCode errorCode) {
        if (this.param == null) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureNumberExtension isNull(MallPcExceptionCode errorCode) {
        if (this.param != null) {
            throw new BizException(errorCode);
        }
        return this;
    }

}
