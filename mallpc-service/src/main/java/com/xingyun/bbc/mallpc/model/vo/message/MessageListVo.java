package com.xingyun.bbc.mallpc.model.vo.message;

import com.xingyun.bbc.mallpc.model.vo.ImageVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Description 消息列表对象 - 视图对象
 * @ClassName MessageListVo
 * @Author ming.yiFei
 * @Date 2019/12/20 18:05
 **/
@Data
public class MessageListVo {

    @ApiModelProperty(value = "消息id")
    private Long messageId;

    @ApiModelProperty(value = "消息标题")
    private String title;

    @ApiModelProperty(value = "消息描述")
    private String desc;

    @ApiModelProperty(value = "消息接收时间")
    private Date receiveTime;

    @ApiModelProperty(value = "消息简介图片")
    private ImageVo imageUrl;

    @ApiModelProperty(value = "消息是否已读 0未读 1已读")
    private Integer isRead;

    @ApiModelProperty(value = "消息推送类型 0手动 1自动")
    private Integer pushType;

    @ApiModelProperty(value = "消息类型 1发货单发货 2注册成功 3修改绑定手机号 4优惠券到账 5优惠券将要过期 6认证成功 7发货提醒 8售后消息 9行云公告 10商品消息")
    private Integer messageType;

    @ApiModelProperty(value = "跳转类型 1该订单对应发货单的物流轨迹 2我的->认证页面 3个人信息页 4 我的->优惠券->未使用页签 5认证成功页面 6公告详情 7商品详情")
    private Integer redirectType;

    @ApiModelProperty(value = "消息信息")
    private MessageSelfInfoVo selfInfoVo;

}
