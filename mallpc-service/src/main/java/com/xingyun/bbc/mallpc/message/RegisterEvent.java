package com.xingyun.bbc.mallpc.message;

import org.springframework.context.ApplicationEvent;

/**
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
