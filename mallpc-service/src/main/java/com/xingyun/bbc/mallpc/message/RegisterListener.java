package com.xingyun.bbc.mallpc.message;

import com.xingyun.bbc.core.operate.enums.PushTypeEnum;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.message.business.MessagePushChannel;
import com.xingyun.bbc.message.business.WaitSendInfo;
import com.xingyun.bbc.message.model.dto.MsgPushDto;
import com.xingyun.bbc.message.model.dto.MsgTemplateVariableDto;
import io.jsonwebtoken.lang.Assert;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: xuxianbei
 * Date: 2019/12/24
 * Time: 14:11
 * Version:V1.0
 */
@Component
public class RegisterListener implements ApplicationListener<RegisterEvent> {

    @Resource
    MessagePushChannel registerChannel;


    @Override
    public void onApplicationEvent(RegisterEvent event) {
        MsgPushDto msgPushDto = new MsgPushDto();
        MsgTemplateVariableDto msgTemplateVariableDto = new MsgTemplateVariableDto();
        Assert.isTrue(event.getSource() instanceof WaitSendInfo, MallPcExceptionCode.SYSTEM_ERROR.getMsg());
        WaitSendInfo waitSendInfo =  (WaitSendInfo) event.getSource();
        msgTemplateVariableDto.setFmobile(waitSendInfo.getOldKey());
        msgPushDto.setMsgTemplateVariable(msgTemplateVariableDto);
        msgPushDto.setPushType(PushTypeEnum.SYSTEM_NOTIFY.getKey());
        //平台会员
        msgPushDto.setSubjectType(1);
        msgPushDto.setSubjectId(waitSendInfo.getTargetId());
        Message<Object> message = MessageBuilder.withPayload(event.getSource()).build();
        registerChannel.systemNoticeOut().send(message);
    }
}
