package com.xingyun.bbc.mallpc.common.ensure.extensions;


import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-18
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class EnsureEnumExtension {

    private Enum param;

    public EnsureEnumExtension(Enum param) {
        this.param = param;
    }

    public EnsureEnumExtension isEqual(Enum param, MallPcExceptionCode errorCode) {
        if (this.param != param) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureEnumExtension isNotEqual(Enum param, MallPcExceptionCode errorCode) {
        if (this.param == param) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureEnumExtension isNotNull(MallPcExceptionCode errorCode) {
        if (this.param == null) {
            throw new BizException(errorCode);
        }
        return this;
    }

}
