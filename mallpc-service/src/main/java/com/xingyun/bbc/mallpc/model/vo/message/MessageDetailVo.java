package com.xingyun.bbc.mallpc.model.vo.message;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Description 消息详情信息 - 视图对象
 * @ClassName MessageDetailVo
 * @Author ming.yiFei
 * @Date 2019/12/20 18:23
 **/
@Data
public class MessageDetailVo {

    @ApiModelProperty(value = "消息内容")
    private String messageContent;

    @ApiModelProperty(value = "消息创建时间")
    private Date messageTime;

    @ApiModelProperty(value = "消息标题")
    private String messageTitle;

    public MessageDetailVo(String messageContent, Date messageTime, String messageTitle) {
        this.messageContent = messageContent;
        this.messageTime = messageTime;
        this.messageTitle = messageTitle;
    }
}
