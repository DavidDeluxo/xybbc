package com.xingyun.bbc.mall.common.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-25
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class ApplicationContextHelper implements ApplicationContextAware {

    private static ApplicationContext context;

    private ApplicationContextHelper() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static ApplicationContext getContext() {
        return context;
    }

}
