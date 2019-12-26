package com.xingyun.bbc.mall.infrastructure.message.interceptor.register;

import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.message.business.MessagePushChannel;
import com.xingyun.bbc.message.business.WaitSendInfo;
import com.xingyun.bbc.message.model.dto.MsgPushDto;
import com.xingyun.bbc.message.model.dto.MsgTemplateVariableDto;
import com.xingyun.bbc.message.model.enums.PushTypeEnum;
import io.jsonwebtoken.lang.Assert;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 注册监听
 *
 * @author: xuxianbei
 * Date: 2019/12/24
 * Time: 14:11
 * Version:V1.0
 */
@Component
@EnableBinding(MessagePushChannel.class)
public class RegisterListener implements ApplicationListener<RegisterEvent> {

    @Resource
    MessagePushChannel registerChannel;


    @Override
    public void onApplicationEvent(RegisterEvent event) {
        MsgPushDto msgPushDto = new MsgPushDto();
        MsgTemplateVariableDto msgTemplateVariableDto = new MsgTemplateVariableDto();
        Assert.isTrue(event.getSource() instanceof WaitSendInfo, MallExceptionCode.SYSTEM_ERROR.getCode());
        WaitSendInfo waitSendInfo = (WaitSendInfo) event.getSource();
        msgTemplateVariableDto.setFmobile(waitSendInfo.getOldKey());
        msgPushDto.setMsgTemplateVariable(msgTemplateVariableDto);
        msgPushDto.setPushType(PushTypeEnum.SYSTEM_NOTIFY.getKey());
        //平台会员
        msgPushDto.setSubjectType(1);
        msgPushDto.setSubjectId(waitSendInfo.getTargetId());
        Message<MsgPushDto> message = MessageBuilder.withPayload(msgPushDto).build();
        registerChannel.systemNoticeOut().send(message);
    }
}
