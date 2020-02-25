package com.xingyun.bbc.mall.service.impl;

import com.google.common.collect.Lists;
import com.xingyun.bbc.core.user.api.UserFeedbackApi;
import com.xingyun.bbc.core.user.enums.UserFeedbackEnums;
import com.xingyun.bbc.core.user.po.UserFeedback;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.RandomUtils;
import com.xingyun.bbc.mall.common.constans.MallConstants;
import com.xingyun.bbc.mall.common.ensure.Ensure;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.common.lock.XybbcLock;
import com.xingyun.bbc.mall.model.dto.UserFeedBackDto;
import com.xingyun.bbc.mall.model.vo.UserFeedBackVo;
import com.xingyun.bbc.mall.service.UserFeedBackService;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class UserFeedBackServiceImpl implements UserFeedBackService {

    public static final Logger logger = LoggerFactory.getLogger(UserFeedBackService.class);

    @Resource
    private UserFeedbackApi userFeedbackApi;

    @Resource
    private Mapper dozerMapper;

    @Resource
    private XybbcLock xybbcLock;

    @Override
    public Result<List<UserFeedBackVo>> getUserFeedBackType() {
        List<UserFeedBackVo> result = new ArrayList<>();
        UserFeedbackEnums.FeedbackTypeEnum[] values = UserFeedbackEnums.FeedbackTypeEnum.values();
        for (UserFeedbackEnums.FeedbackTypeEnum value : values) {
            UserFeedBackVo userFeedBackVo = new UserFeedBackVo();
            userFeedBackVo.setFfeedbackType(value.getValue());
            userFeedBackVo.setFfeedbackTypeStr(value.getName());
            userFeedBackVo.setFfeedbackTypeInfor(value.getInfor());
            result.add(userFeedBackVo);
        }
        return Result.success(result);
    }

    @Override
    public Result saveUserFeedBack(UserFeedBackDto dto) {
        String lockKey = StringUtils.join(Lists.newArrayList(MallConstants.USER_FEEDBACK_LOCK, dto.getFuid(), dto.getFfeedbackType()), ":");
        String lockValue = RandomUtils.getUUID();
        try {
            Ensure.that(xybbcLock.tryLockTimes(lockKey, lockValue, 3, 5)).isTrue(MallExceptionCode.SYSTEM_BUSY_ERROR);
            UserFeedback feedback = dozerMapper.map(dto, UserFeedback.class);
            feedback.setFfeedbackStatus(0);
            feedback.setFisCollect(0);
            feedback.setFisDelete(0);
            feedback.setFfeedbackPic(Objects.isNull(dto.getFfeedbackPiclis()) ? "" : StringUtils.join(dto.getFfeedbackPiclis(), ","));
            Result<Integer> saveResult = userFeedbackApi.create(feedback);
            Ensure.that(saveResult.isSuccess()).isTrue(new MallExceptionCode(saveResult.getCode(), saveResult.getMsg()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            xybbcLock.releaseLock(lockKey, lockValue);
        }
        return Result.success(true);
    }
}
