package com.xingyun.bbc.mall.infrastructure.message.interceptor.register;

import com.alibaba.fastjson.JSONObject;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.ResultUtils;
import com.xingyun.bbc.mall.model.dto.UserRegisterDto;
import com.xingyun.bbc.message.business.NotifyBusinessInterface;
import com.xingyun.bbc.message.business.impl.AbstractNotifyBusiness;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 手机注册消息
 *
 * @author: xuxianbei
 * Date: 2019/12/24
 * Time: 13:44
 * Version:V1.0
 */
@Component
public class RegisterMessage extends AbstractNotifyBusiness implements NotifyBusinessInterface {

    /**
     * 业务ID
     * @return
     */
    private String businessId;

    /**
     * 旧的key
     */
    private String oldKey;

    /**
     * 新的key
     */
    private String newKey;

    /**
     * 目标id;平台会员ID fuid /商家ID
     */
    private Long targetId;

    @Resource
    private UserApi userApi;


    @Override
    public String getBusinessId() {
        return this.businessId;
    }

    @Override
    public Long getTargetId() {
        return this.targetId;
    }

    @Override
    public String getOldKey() {
        return this.oldKey;
    }

    @Override
    public String getNewKey() {
        return this.newKey;
    }


    @Override
    public String[] getUris() {
        return new String[]{"/user/via/register"};
    }

    @Override
    public ApplicationEvent getApplicationEvent() {
        return new RegisterEvent(waitSendInfo);
    }

    @Override
    protected void doSetKey() {
        UserRegisterDto userRegisterDto = JSONObject.parseObject(requestBody, UserRegisterDto.class);
        this.newKey = userRegisterDto.getFmobile();
        User user = new User();
        user.setFmobile(newKey);
        Result<User> userResult = userApi.queryOne(user);
        user = ResultUtils.getData(userResult);
        targetId = user.getFuid();
    }
}
