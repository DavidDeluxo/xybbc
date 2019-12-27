package com.xingyun.bbc.mallpc.infrastructure.interceptor.register;

import com.alibaba.fastjson.JSONObject;
import com.xingyun.bbc.core.operate.enums.TemplateTypeEnum;
import com.xingyun.bbc.message.business.MessagePushChannel;
import com.xingyun.bbc.message.business.WaitSendInfo;
import com.xingyun.bbc.message.model.dto.MsgPushDto;
import com.xingyun.bbc.message.model.dto.MsgTemplateVariableDto;
import com.xingyun.bbc.message.model.enums.PushTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
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
@Slf4j
@Component
@EnableBinding(MessagePushChannel.class)
public class RegisterMessage {

    @Resource
    private MessagePushChannel registerChannel;


    public void onApplicationEvent(WaitSendInfo waitSendInfo) {
        MsgPushDto msgPushDto = new MsgPushDto();
        MsgTemplateVariableDto msgTemplateVariableDto = new MsgTemplateVariableDto();
        msgTemplateVariableDto.setFmobile(waitSendInfo.getBusinessId());
        msgPushDto.setMsgTemplateVariable(msgTemplateVariableDto);
        msgPushDto.setSystemTemplateType(TemplateTypeEnum.REGISTER_SUCCESSED.getKey());
        msgPushDto.setPushType(PushTypeEnum.SYSTEM_NOTIFY.getKey());
        //平台会员
        msgPushDto.setSubjectType(1);
        msgPushDto.setSubjectId(waitSendInfo.getTargetId());
        Message<MsgPushDto> message = MessageBuilder.withPayload(msgPushDto).build();
        boolean result = registerChannel.systemNoticeOut().send(message);
        if (result) {
            log.info("消息发送成功" + JSONObject.toJSONString(message));
        }
    }
}
