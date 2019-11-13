package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.*;
import com.xingyun.bbc.mall.model.vo.*;

import java.util.List;

/**
 * @author ZSY
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
public interface UserService {
    Result<UserLoginVo> userLogin(UserLoginDto dto);

    Result<SendSmsVo> sendSmsAuthNum(UserLoginDto dto);

    Result<Integer> checkPAuthNum(UserLoginDto dto);

    Result<UserLoginVo> registerUser(UserRegisterDto dto);

    Result<Integer> forgotPwd(UserRegisterDto dto);

    Result<Integer> userVerify(UserVerifyDto dto);

    Result<UserVerifyVo> queryUserVerify(UserVerifyDto dto);

    Result<List<VerifyCategoryVo>> queryCategory();

    Result<List<VerifyPlatformVo>> queryPlatform();

    Result<UserVo> queryUserVerifyStatus(Long fuid);

    Result<SendSmsVo> accountSecurityVerification(UserSecurityDto dto);

    Result<Integer> checkEmailAuthNum(UserSecurityDto dto);

    Result<Integer> modifiyPayPwd(UserSecurityDto dto);

    Result<Integer> modifiyPwd(UserSecurityDto dto);

    Result<Integer> modifiyEmailAccount(UserSecurityDto dto);

    Result<SendSmsVo> modifiyMobileSendSMS(UserSecurityDto dto);

    Result<Integer> modifiyMobile(UserSecurityDto dto);

    Result<UserVo> queryUserInfo(Long fuid);

    Result<Integer> modifiyUserNickname(UserDto dto);

    Result<UserVo> queryPopupWindowsStatus(Long fuid);

    Result couponLinkReceive(CouponLinkDto dto);
    
    /**
    * 
    * @Title: getUnusedCouponCount 
    * @Description: 获取未使用的优惠卷数量
    * @param @param fuid
    * @param @return  参数说明 
    * @return Result<Integer>    返回类型 
    * @author feixiaojie
    * @throws
     */
    Result<Integer>getUnusedCouponCount(Long fuid);
}

