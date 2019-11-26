package com.xingyun.bbc.mallpc.common.ensure.extensions;

import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Objects;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-18
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class EnsureCollectionExtension {

    private Collection param;

    public EnsureCollectionExtension(Collection param) {
        this.param = param;
    }

    public EnsureCollectionExtension isNotEmpty(MallPcExceptionCode errorCode) {
        if (!CollectionUtils.isEmpty(this.param)) {
            return this;
        }
        throw new BizException(errorCode);
    }

    public EnsureCollectionExtension isNotNull(MallPcExceptionCode errorCode) {
        if (this.param == null) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureCollectionExtension isEqualTo(Collection param, MallPcExceptionCode errorCode) {
        if (!Objects.equals(this.param, param)) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureCollectionExtension isNotEqualTo(Collection param, MallPcExceptionCode errorCode) {
        if (Objects.equals(this.param, param)) {
            throw new BizException(errorCode);
        }
        return this;
    }

}
