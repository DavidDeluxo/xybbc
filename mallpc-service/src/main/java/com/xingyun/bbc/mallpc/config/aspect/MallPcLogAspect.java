/*
package com.xingyun.bbc.mallpc.config.aspect;

import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.MallPcLogHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

*/
/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-10-22
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 *//*

@Aspect
@Component
@Slf4j
@Order(1)
public class MallPcLogAspect {

    @Pointcut("execution(public * com.xingyun.bbc.mallpc.controller..*.*(..))")
    private void allMethod() {
    }

    @Around("allMethod()")
    public Object doAround(ProceedingJoinPoint joinPoint) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Result result;
        try {
            result = (Result) joinPoint.proceed();
        } catch (BizException e) {
            log.error("catch business exception", e);
            result = Result.failure(e.getStatus().getCode(), e.getStatus().getMsg());
        } catch (IllegalArgumentException e) {
            log.error("catch business exception", e);
            result = Result.failure(MallPcExceptionCode.PARAM_ERROR.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("catch system exception", e);
            result = Result.failure(MallPcExceptionCode.SYSTEM_ERROR);
        }
        stopWatch.stop();
        MallPcLogHelper.getInstance().outputLog(result, ArrayUtils.isNotEmpty(joinPoint.getArgs()) ? joinPoint.getArgs()[0] : null, stopWatch.getTotalTimeMillis());
        return result;
    }

}
*/
