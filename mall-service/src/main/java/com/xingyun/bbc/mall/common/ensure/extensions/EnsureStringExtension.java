package com.xingyun.bbc.mall.common.ensure.extensions;


import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import org.apache.commons.lang3.StringUtils;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-18
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class EnsureStringExtension {

    private String param;

    public EnsureStringExtension(String param) {
        this.param = param;
    }

    public EnsureStringExtension isNotNull(MallExceptionCode errorCode) {
        if (this.param == null) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureStringExtension isNotEmpty(MallExceptionCode errorCode) {
        if (StringUtils.isEmpty(this.param)) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureStringExtension isNotBlank(MallExceptionCode errorCode) {
        if (StringUtils.isBlank(this.param)) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureStringExtension isEqualTo(String param, MallExceptionCode errorCode) {
        if (!StringUtils.equals(this.param, param)) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureStringExtension isNotEqualTo(String param, MallExceptionCode errorCode) {
        if (StringUtils.equals(this.param, param)) {
            throw new BizException(errorCode);
        }
        return this;
    }

}
