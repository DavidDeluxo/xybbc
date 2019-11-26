package com.xingyun.bbc.mallpc.common.ensure.extensions;

import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Objects;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-09-12
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class EnsureResultExtension {

    private Result param;

    public EnsureResultExtension(Result param) {
        this.param = param;
    }

    public EnsureResultExtension isNotNull(MallPcExceptionCode errorCode) {
        if (this.param == null) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureResultExtension isNotNullData(MallPcExceptionCode errorCode) {
        Ensure.that(param).isNotNull(MallPcExceptionCode.SYSTEM_ERROR);
        Ensure.that(param.isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
        if (Objects.isNull(this.param.getData())) {
            throw new BizException(errorCode);
        }
        return this;
    }

    public EnsureResultExtension isNotEmptyData(MallPcExceptionCode errorCode) {
        Ensure.that(param).isNotNull(MallPcExceptionCode.SYSTEM_ERROR);
        Ensure.that(param.isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
        if (CollectionUtils.sizeIsEmpty(this.param.getData())) {
            throw new BizException(errorCode);
        }
        return this;
    }

    /**
     * @param errorCode
     * @return
     */
    public EnsureResultExtension isSuccess(MallPcExceptionCode errorCode) {
        Ensure.that(param).isNotNull(MallPcExceptionCode.SYSTEM_ERROR);
        if (param.isSuccess()) {
            return this;
        }
        throw new BizException(errorCode);
    }

    /**
     * 该断言的业务场景为：写入数据库影响的行数大于0，如果不关注影响的行数大于0则不应该调用此方法
     *
     * @param errorCode
     * @return
     */
    public EnsureResultExtension writeIsSuccess(MallPcExceptionCode errorCode) {
        Ensure.that(param).isNotNull(MallPcExceptionCode.SYSTEM_ERROR);
        if (param.isSuccess() && Objects.nonNull(param.getData()) && (Integer) param.getData() > 0) {
            return this;
        }
        throw new BizException(errorCode);
    }

}
