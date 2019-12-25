package com.xingyun.bbc.mall.infrastructure.message.interceptor.mobile;

import org.springframework.context.ApplicationEvent;

/**
 * 修改手机号码
 * @author: xuxianbei
 * Date: 2019/12/25
 * Time: 20:09
 * Version:V1.0
 */
public class ModifyMobileEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public ModifyMobileEvent(Object source) {
        super(source);
    }
}
