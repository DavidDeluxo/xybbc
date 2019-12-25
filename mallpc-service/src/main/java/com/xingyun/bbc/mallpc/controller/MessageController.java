package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.utils.RequestHolder;
import com.xingyun.bbc.mallpc.model.dto.message.MessageQueryDto;
import com.xingyun.bbc.mallpc.model.dto.message.MessageUpdateDto;
import com.xingyun.bbc.mallpc.model.vo.PageVo;
import com.xingyun.bbc.mallpc.model.vo.message.MessageCenterVo;
import com.xingyun.bbc.mallpc.model.vo.message.MessageDetailVo;
import com.xingyun.bbc.mallpc.model.vo.message.MessageListVo;
import com.xingyun.bbc.mallpc.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description 消息中心
 * @ClassName MessageController
 * @Author ming.yiFei
 * @Date 2019/12/20 17:30
 **/
@Api("消息中心 - 控制层")
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @ApiOperation("查询消息中心")
    @PostMapping(value = "/queryMessageGroupByUserId")
    public Result<List<MessageCenterVo>> queryMessageGroupByUserId(){
        return messageService.queryMessageGroupByUserId(RequestHolder.getUserId());
    }

    @ApiOperation("查询消息列表")
    @PostMapping(value = "/queryMessageList")
    public Result<PageVo<MessageListVo>> queryMessageList(@RequestBody MessageQueryDto dto){
        Assert.isTrue(dto.getMessageCenterType() != null, "消息分组类型不能为空");
        dto.setUserId(RequestHolder.getUserId());
        return messageService.queryMessageList(dto);
    }

    @ApiOperation("查询消息详情")
    @PostMapping(value = "/queryMessageDetailById")
    public Result<MessageDetailVo> queryMessageDetailById(@RequestBody MessageQueryDto dto){
        dto.setUserId(RequestHolder.getUserId());
        return messageService.queryMessageDetailById(dto);
    }

    @ApiOperation("更新消息已读")
    @PostMapping(value = "/updateMessageForRead")
    public Result updateMessageForRead(@RequestBody MessageUpdateDto dto){
        dto.setUserId(RequestHolder.getUserId());
        return messageService.updateMessageForRead(dto);
    }

}
