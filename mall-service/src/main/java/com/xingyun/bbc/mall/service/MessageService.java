package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.MessageQueryDto;
import com.xingyun.bbc.mall.model.dto.MessageUpdateDto;
import com.xingyun.bbc.mall.model.vo.MessageCenterVo;
import com.xingyun.bbc.mall.model.vo.MessageDetailVo;
import com.xingyun.bbc.mall.model.vo.MessageListVo;
import com.xingyun.bbc.mall.model.vo.PageVo;

import java.util.List;

/**
 * @Description 消息中心 - 接口
 * @ClassName MessageService
 * @Author ming.yiFei
 * @Date 2019/12/23 11:36
 **/
public interface MessageService {

    /**
     * @Description 查询消息中心
     * @Author ming.yiFei
     * @Date 11:39 2019/12/23
     * @Param [userId]
     * @return com.xingyun.bbc.core.utils.Result<java.util.List<com.xingyun.bbc.mall.model.vo.MessageCenterVo>>
     **/
    Result<List<MessageCenterVo>> queryMessageGroupByUserId(Long userId);

    /**
     * @Description 查询消息列表
     * @Author ming.yiFei
     * @Date 11:39 2019/12/23
     * @Param [dto]
     * @return com.xingyun.bbc.core.utils.Result<com.xingyun.bbc.mall.model.vo.PageVo<com.xingyun.bbc.mall.model.vo.MessageListVo>>
     **/
    Result<PageVo<MessageListVo>> queryMessageList(MessageQueryDto dto);

    /**
     * @Description 查询消息详情
     * @Author ming.yiFei
     * @Date 11:39 2019/12/23
     * @Param [dto]
     * @return com.xingyun.bbc.core.utils.Result<com.xingyun.bbc.mall.model.vo.MessageDetailVo>
     **/
    Result<MessageDetailVo> queryMessageDetailById(MessageQueryDto dto);

    /**
     * @Description 更新消息已读
     * @Author ming.yiFei
     * @Date 11:40 2019/12/23
     * @Param [dto]
     * @return com.xingyun.bbc.core.utils.Result
     **/
    Result updateMessageForRead(MessageUpdateDto dto);

    /**
     * @Description 统计未读消息数量
     * @Author ming.yiFei
     * @Date 11:22 2020/1/6
     * @Param [userId]
     * @return com.xingyun.bbc.core.utils.Result<java.lang.Integer>
     **/
    Result<Integer> countMessageForUnRead(Long userId);
}
