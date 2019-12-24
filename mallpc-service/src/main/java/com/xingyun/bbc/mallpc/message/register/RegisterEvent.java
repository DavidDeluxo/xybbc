package com.xingyun.bbc.mallpc.message.register;

import org.springframework.context.ApplicationEvent;

/**
 * 注册事件
 * @author: xuxianbei
 * Date: 2019/12/24
 * Time: 14:10
 * Version:V1.0
 */
public class RegisterEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public RegisterEvent(Object source) {
        super(source);
    }
}
