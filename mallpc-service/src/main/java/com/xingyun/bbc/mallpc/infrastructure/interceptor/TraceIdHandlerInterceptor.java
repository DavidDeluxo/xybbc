package com.xingyun.bbc.mallpc.infrastructure.interceptor;

import brave.Tracer;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description
 * @ClassName TraceIdInterceptor
 * @Author ming.yiFei
 * @Date 2020/1/13 11:05
 **/
@Component
public class TraceIdHandlerInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private Tracer tracer;

    private static final Logger log = LoggerFactory.getLogger(TraceIdHandlerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {

        String xid = RootContext.getXID();
        String traceId = tracer.currentSpan().context().traceIdString();
        log.info("事务ID = {}, traceId = {}", xid, traceId);
        return true;
    }
}
