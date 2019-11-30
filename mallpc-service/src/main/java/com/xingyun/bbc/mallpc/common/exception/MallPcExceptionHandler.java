package com.xingyun.bbc.mallpc.common.exception;

import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 *
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-30
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@RestControllerAdvice
@Slf4j
public class MallPcExceptionHandler {

    @ExceptionHandler(value = Throwable.class)
    public Object returnErrorCode(Throwable exception) {
        if (exception instanceof MissingServletRequestParameterException) {
            return Result.failure(MallPcExceptionCode.REQUIRED_PARAM_MISSING.getCode(), MallPcExceptionCode.REQUIRED_PARAM_MISSING.getMsg());
        } else if (exception instanceof IllegalArgumentException) {
            return Result.failure(MallPcExceptionCode.PARAM_ERROR.getCode(), exception.getMessage());
        } else if (exception instanceof BindException) {
            FieldError fieldError = ((BindException) exception).getBindingResult().getFieldError();
            return Result.failure(MallPcExceptionCode.PARAM_ERROR.getCode(), fieldError.getDefaultMessage());
        } else if (exception instanceof MethodArgumentNotValidException) {
            FieldError fieldError = ((MethodArgumentNotValidException) exception).getBindingResult().getFieldError();
            return Result.failure(MallPcExceptionCode.PARAM_ERROR.getCode(), fieldError.getDefaultMessage());
        } else if (exception instanceof BizException) {
            return Result.failure(((BizException) exception).getStatus());
        } else {
            log.error("catch global exception", exception);
            return Result.failure(MallPcExceptionCode.SYSTEM_ERROR);
        }
    }

}
