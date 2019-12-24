package com.xingyun.bbc.mallpc.message.register;

import com.alibaba.fastjson.JSONObject;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.utils.ResultUtils;
import com.xingyun.bbc.mallpc.model.dto.user.UserRegisterDto;
import com.xingyun.bbc.message.business.NotifyBusinessInterface;
import com.xingyun.bbc.message.business.impl.AbstractNotifyBusiness;
import io.jsonwebtoken.lang.Assert;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;

/**
 * 手机更新接收
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

    private String oldKey;

    private String newKey;

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
        Assert.isTrue(requestMethod == RequestMethod.POST, "");
        UserRegisterDto userRegisterDto = JSONObject.parseObject(requestBody, UserRegisterDto.class);
        this.newKey = userRegisterDto.getFmobile();
        User user = new User();
        user.setFmobile(newKey);
        Result<User> userResult = userApi.queryOne(user);
        user = ResultUtils.getData(userResult);
        targetId = user.getFuid();
    }
}
