package com.xingyun.bbc.mallpc.infrastructure.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @Description
 * @ClassName TraceIdHandlerInterceptorConfiguration
 * @Author ming.yiFei
 * @Date 2020/1/16 14:24
 **/
@Configuration
public class TraceIdHandlerInterceptorConfiguration  implements WebMvcConfigurer {

    @Resource
    private TraceIdHandlerInterceptor traceIdHandlerInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(traceIdHandlerInterceptor).addPathPatterns("/**");
    }
}
