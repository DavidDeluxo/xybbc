package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.user.SendSmsCodeDto;
import com.xingyun.bbc.mallpc.model.dto.user.UserLoginDto;
import com.xingyun.bbc.mallpc.model.dto.user.UserRegisterDto;
import com.xingyun.bbc.mallpc.model.vo.user.SendSmsCodeVo;
import com.xingyun.bbc.mallpc.model.vo.user.UserLoginVo;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-19
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public interface UserService {

    /**
     * @author nick
     * @date 2019-11-19
     * @description :  用户登录
     * @version 1.0.0
     */
     Result<UserLoginVo> userLogin(UserLoginDto userLoginDto);

     /**
      * @author nick
      * @date 2019-11-19
      * @description :  自动登录
      * @version 1.0.0
      */
    Result<UserLoginVo> autoLogin();

    /**
     * @author nick
     * @date 2019-11-19
     * @description :  注册
     * @version 1.0.0
     */
    Result<UserLoginVo> register(UserRegisterDto userRegisterDto);

    /**
     * @author nick
     * @date 2019-11-19
     * @description :  发送验证码
     * @version 1.0.0
     */
    Result<SendSmsCodeVo> sendSmsCode(SendSmsCodeDto sendSmsCodeDto);

    /**
     * @author nick
     * @date 2019-11-19
     * @description :  重置密码
     * @version 1.0.0
     */
    Result resetPwd(SendSmsCodeDto sendSmsCodeDto);
}
