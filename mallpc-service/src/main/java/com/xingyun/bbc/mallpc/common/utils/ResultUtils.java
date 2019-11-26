package com.xingyun.bbc.mallpc.common.utils;

import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 结果工具类
 * User: xuxianbei
 * Date: 2019/8/31
 * Time: 12:14
 * Version:V1.0
 */
@Slf4j
public class ResultUtils {

    /**
     * 从result中获取data
     *
     * @param result
     * @return
     */
    public static <T> T getData(Result<T> result) {
        if (!result.isSuccess()) {
            log.error("远程服务调用错误信息 ： " + result.getMsg());
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        return result.getData();
    }

    /**
     * 从result中获取data
     * 数据不存在报指定异常
     *
     * @param result
     * @return
     */
    public static <T> T getDataNotNull(Result<T> result, MallPcExceptionCode mallPcExceptionCode) {
        T data = getData(result);
        if (data == null) {
            throw new BizException(mallPcExceptionCode);
        }
        return data;
    }

    /**
     * 从result中获取data
     * 数据不存在报指定异常
     *
     * @param result
     * @return
     */
    public static <T> T getDataNotNull(Result<T> result) {
        T data = getData(result);
        if (data == null) {
            throw new BizException(MallPcExceptionCode.RECORD_NOT_EXIST);
        }
        return data;
    }

    /**
     * 从result中获取data
     * 数据不存在报指定异常
     *
     * @param result
     * @return
     */
    public static <T> List<T> getListNotEmpty(Result<List<T>> result, MallPcExceptionCode mallPcExceptionCode) {
        if (!result.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        List<T> data = result.getData();
        if (CollectionUtils.isEmpty(data)) {
            throw new BizException(mallPcExceptionCode);
        }
        return data;
    }

    /**
     * 从result中获取data
     * 数据不存在报指定异常
     *
     * @param result
     * @return
     */
    public static <T> List<T> getListNotEmpty(Result<List<T>> result) {
        if (!result.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        List<T> data = result.getData();
        if (CollectionUtils.isEmpty(data)) {
            throw new BizException(MallPcExceptionCode.RECORD_NOT_EXIST);
        }
        return data;
    }

    /**
     * 是否更新成功
     *
     * @param result
     */
    public static void isUpdateSuccess(Result<Integer> result) {
        if (!result.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        Integer count = result.getData();
        if (count == null || count != 1) {
            throw new BizException(MallPcExceptionCode.UPDATE_FAILED);
        }
    }

    /**
     * 是否更新成功
     *
     * @param result
     * @param targetSize
     */
    public static void isUpdateSuccess(Result<Integer> result, int targetSize) {
        if (!result.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        Integer count = result.getData();
        if (count == null || count != targetSize) {
            throw new BizException(MallPcExceptionCode.UPDATE_FAILED);
        }
    }
}
