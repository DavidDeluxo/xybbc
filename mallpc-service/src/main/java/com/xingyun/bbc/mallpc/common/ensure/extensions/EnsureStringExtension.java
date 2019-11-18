package com.xingyun.bbc.mallpc.common.ensure.extensions;

import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
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

    public EnsureStringExtension isNotNull(MallPcExceptionCode errorCode) {
        if (this.param == null) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureStringExtension isNotEmpty(MallPcExceptionCode errorCode) {
        if (StringUtils.isEmpty(this.param)) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureStringExtension isNotBlank(MallPcExceptionCode errorCode) {
        if (StringUtils.isBlank(this.param)) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureStringExtension isEqualTo(String param, MallPcExceptionCode errorCode) {
        if (!StringUtils.equals(this.param, param)) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureStringExtension isNotEqualTo(String param, MallPcExceptionCode errorCode) {
        if (StringUtils.equals(this.param, param)) {
            throw new BizException(errorCode);
        }
        return this;
    }

}
