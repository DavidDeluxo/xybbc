package com.xingyun.bbc.mall.message.modifymobile;

import com.alibaba.fastjson.JSONObject;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.ResultUtils;
import com.xingyun.bbc.mall.model.dto.UserSecurityDto;
import com.xingyun.bbc.message.business.NotifyBusinessInterface;
import com.xingyun.bbc.message.business.impl.AbstractNotifyBusiness;
import org.springframework.context.ApplicationEvent;

import javax.annotation.Resource;

/**
 * 修改手机消息
 *
 * @author: xuxianbei
 * Date: 2019/12/25
 * Time: 17:54
 * Version:V1.0
 */
//@Component
public class ModifyMobileMessage extends AbstractNotifyBusiness implements NotifyBusinessInterface {

    /**
     * 平台会员ID，或者供应商Id
     */
    private Long targetId;

    /**
     * 业务id：采购Id，优惠券ID 就是表主键
     */
    private String businessId;

    /**
     * 新的状态；如果是手机号码，这里是旧手机号码； 如果是采购单从待发货变成待收货。这里是待收货
     */
    private String newKey;

    /**
     * 旧的状态；如果是手机号码，这里是新手机号码  如果是采购单从待发货变成待收货。这里是待发货
     */
    private String oldKey;

    @Resource
    private UserApi userApi;

    /**
     * 请求之前调用
     */
    @Override
    protected void doSetKey() {
        UserSecurityDto userSecurityDto = JSONObject.parseObject(requestBody, UserSecurityDto.class);
        Long fuid = userSecurityDto.getFuid();
        String mobile = userSecurityDto.getFmobile();
        User user = new User();
        user.setFuid(fuid);
        Result<User> resultUser = userApi.queryOne(user);
        user = ResultUtils.getData(resultUser);
        user.getFmobile();
        newKey = mobile;
        oldKey = user.getFmobile();
        //如果是采购单，这里就是采购单id
        businessId = String.valueOf(user.getFuid());
        targetId = user.getFuid();
    }

    @Override
    public String getBusinessId() {
        return null;
    }

    @Override
    public Long getTargetId() {
        return null;
    }

    @Override
    public String getOldKey() {
        return null;
    }

    @Override
    public String getNewKey() {
        return null;
    }

    @Override
    public String[] getUris() {
        return new String[0];
    }

    @Override
    public ApplicationEvent getApplicationEvent() {
        return null;
    }
}
