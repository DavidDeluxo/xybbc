package com.xingyun.bbc.mallpc.common.ensure;

import cn.hutool.core.util.ObjectUtil;
import com.xingyun.bbc.core.enums.IResultStatus;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.utils.Result;

/**
 * @author pengaoluo
 * @version 1.0.0
 */
public class EnsureHelper {

    /**
     * 校验远程调用结果,如果data属性为空,则抛出远程服务调用异常
     * 如果无异常,则返回data
     *
     * @param result 远程调用结果
     * @return
     */
    public static <T> T checkDataBe(Result<T> result, IResultStatus resultStatus, Object dataShouldBe) {
        checkSuccess(result);
        T data = result.getData();
        if (ObjectUtil.notEqual(data, dataShouldBe)) {
            throw new BizException(resultStatus);
        }
        return data;
    }

    /**
     * 校验远程调用结果,如果data属性为空,则抛出远程服务调用异常
     * 如果无异常,则返回data
     *
     * @param result 远程调用结果
     * @return
     */
    public static <T> T checkDataNotBe(Result<T> result, IResultStatus resultStatus, Object dataShouldNotBe) {
        checkSuccess(result);
        T data = result.getData();
        if (ObjectUtil.equal(data, dataShouldNotBe)) {
            throw new BizException(resultStatus);
        }
        return data;
    }

    /**
     * 校验远程调用结果,如果data属性为空,则抛出远程服务调用异常
     * 如果无异常,则返回data
     *
     * @param result 远程调用结果
     * @return
     */
    public static <T> T checkNotNullAndGetData(Result<T> result) {
        return checkNotNullAndGetData(result, ResultStatus.REMOTE_SERVICE_ERROR);
    }

    /**
     * 校验远程调用结果,如果data属性为空,则抛出异常
     * 如果无异常,则返回data
     *
     * @param result 远程调用结果
     * @return
     */
    public static <T> T checkNotNullAndGetData(Result<T> result, IResultStatus resultStatus) {
        return checkDataNotBe(result, resultStatus, null);
    }

    /**
     * 校验远程调用是否成功,如果success属性为false,则抛出异常
     * 如果无异常,则返回data
     *
     * @param result 远程调用结果
     * @return
     */
    public static <T> T checkSuccessAndGetData(Result<T> result) {
        checkSuccess(result);
        return result.getData();
    }

    /**
     * 校验远程调用结果
     *
     * @param result
     */
    public static <T> Result<T> checkSuccessAndGetResult(Result<T> result) {
        checkSuccess(result);
        return result;
    }

    public static <T> void checkSuccess(Result<T> result) {
        if (!result.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
    }

}
