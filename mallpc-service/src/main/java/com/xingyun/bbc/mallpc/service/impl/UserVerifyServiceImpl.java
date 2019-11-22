package com.xingyun.bbc.mallpc.service.impl;

import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.api.UserVerifyApi;
import com.xingyun.bbc.core.user.constants.UserRedisConstant;
import com.xingyun.bbc.core.user.enums.UserVerifyStatusEnum;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.user.po.UserVerify;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.components.lock.XybbcLock;
import com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant;
import com.xingyun.bbc.mallpc.common.convertor.TypeConvertor;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.ensure.EnsureHelper;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.RequestHolder;
import com.xingyun.bbc.mallpc.model.dto.user.UserVerifyDTO;
import com.xingyun.bbc.mallpc.model.vo.user.UserVerifyVO;
import com.xingyun.bbc.mallpc.service.UserVerifyService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author pengaoluo
 * @version 1.0.0
 */
@Slf4j
@Service
public class UserVerifyServiceImpl implements UserVerifyService {

    @Resource
    private DozerHolder dozerHolder;

    @Resource
    private UserApi userApi;

    @Resource
    private UserVerifyApi userVerifyApi;

    @Resource
    private XybbcLock xybbcLock;

    @GlobalTransactional
    @Override
    public void verify(UserVerifyDTO dto) {
        Long fuid = RequestHolder.getUserId();
        Criteria<User, Object> userCriteria = Criteria.of(User.class).fields(User::getFuid, User::getFverifyStatus).andEqualTo(User::getFuid, fuid);
        User user = EnsureHelper.checkNotNullAndGetData(userApi.queryOneByCriteria(userCriteria), MallPcExceptionCode.RECORD_NOT_EXIST);
        checkVerifyStatus(user);
        UserVerify userVerifyNew = dozerHolder.convert(dto, UserVerify.class);
        userVerifyNew.setFuid(fuid);
        Criteria<UserVerify, Object> criteria = Criteria.of(UserVerify.class).andEqualTo(UserVerify::getFuid, fuid);
        UserVerify userVerifyOld = EnsureHelper.checkSuccessAndGetData(userVerifyApi.queryOneByCriteria(criteria));
        user.setFoperateType(dto.getFoperateType());
        user.setFverifyStatus(UserVerifyStatusEnum.INAUTHORIZATION.getCode());
        EnsureHelper.checkNotNullAndGetData(userApi.updateNotNull(user));
        if (Objects.isNull(userVerifyOld)) {
            boolean getLock = false;
            String key = UserRedisConstant.USER_VERIFY_CREATE_PREFIX + fuid;
            try {
                Ensure.that(getLock = xybbcLock.tryLock(key, MallPcRedisConstant.DEFAULT_LOCK_VALUE, MallPcRedisConstant.DEFAULT_LOCK_EXPIRING))
                        .isTrue(MallPcExceptionCode.SYSTEM_BUSY_ERROR);
                EnsureHelper.checkSuccess(userVerifyApi.create(userVerifyNew));
            } finally {
                if (getLock) {
                    xybbcLock.releaseLock(key, MallPcRedisConstant.DEFAULT_LOCK_VALUE);
                }
            }
        } else {
            userVerifyNew.setFuserVerifyId(userVerifyOld.getFuserVerifyId());
            EnsureHelper.checkSuccess(userVerifyApi.updateNotNull(userVerifyNew));
        }
    }


    /**
     * 校验用户认证状态,如果是未认证或者是认证失败状态,则可以添加认证,否则报错
     *
     * @param user
     */
    private void checkVerifyStatus(User user) {
        Integer fverifyStatus = user.getFverifyStatus();
        if (!UserVerifyStatusEnum.UNAUTHORIZED.getCode().equals(fverifyStatus) && !UserVerifyStatusEnum.AUTHORIZATION_FAILED.getCode().equals(fverifyStatus)) {
            throw new BizException(MallPcExceptionCode.USER_CANNOT_VERIFY);
        }
    }

    @Override
    public UserVerifyVO view() {
        Long fuid = RequestHolder.getUserId();
        User user = EnsureHelper.checkNotNullAndGetData(userApi.queryById(fuid), MallPcExceptionCode.USER_NOT_EXIST);
        Criteria<UserVerify, Object> criteria = Criteria.of(UserVerify.class).andEqualTo(UserVerify::getFuid, fuid);
        UserVerify userVerify = EnsureHelper.checkSuccessAndGetData(userVerifyApi.queryOneByCriteria(criteria));
        UserVerifyVO userVerifyVO = Objects.isNull(userVerify) ? new UserVerifyVO() : TypeConvertor.convertUserVerifyToUserVerifyVO(userVerify);
        userVerifyVO.setFverifyStatus(user.getFverifyStatus());
        userVerifyVO.setFoperateType(user.getFoperateType());
        return userVerifyVO;
    }
}
