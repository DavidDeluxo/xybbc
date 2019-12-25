package com.xingyun.bbc.mallpc.model.vo.message;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description 消息分组列表 - 视图对象
 * @ClassName MessageCenterVo
 * @Author ming.yiFei
 * @Date 2019/12/20 17:23
 **/
@Data
public class MessageCenterVo {

    @ApiModelProperty(value = "消息分组类型 1物流助手 2账户通知 3优惠促销 4通知消息")
    private Integer messageGroupType;

    @ApiModelProperty(value = "消息分组描述")
    private String messageGroupDesc = "";

    @ApiModelProperty(value = "未读消息数量")
    private Integer unreadMessageCount = 0;

    @ApiModelProperty(value = "最新接收消息时间")
    private Long receiveMessageTime = 0L;

    public MessageCenterVo(Integer messageGroupType) {
        this.messageGroupType = messageGroupType;
    }

    public MessageCenterVo(Integer messageGroupType, String messageGroupDesc, Integer unreadMessageCount, Long receiveMessageTime) {
        this.messageGroupType = messageGroupType;
        this.messageGroupDesc = messageGroupDesc;
        this.unreadMessageCount = unreadMessageCount;
        this.receiveMessageTime = receiveMessageTime;
    }
}
