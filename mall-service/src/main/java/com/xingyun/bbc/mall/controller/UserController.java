package com.xingyun.bbc.mall.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.*;
import com.xingyun.bbc.mall.model.vo.*;
import com.xingyun.bbc.mall.service.GoodDetailService;
import com.xingyun.bbc.mall.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author ZSY
 * @Description: 用户注册登录
 * @createTime: 2019-09-03 11:00
 */
@RestController
@RequestMapping(value = "/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private GoodDetailService goodDetailService;

    @ApiOperation("登陆")
    @PostMapping("/via/userLogin")
    public Result<UserLoginVo> userLogin(@RequestBody UserLoginDto dto) {
        return userService.userLogin(dto);
    }

    @ApiOperation("发送短信")
    @PostMapping("/via/sendSmsAuthNum")
    public Result<SendSmsVo> sendSmsAuthNum(@RequestBody UserLoginDto dto, HttpServletRequest request){
        dto.setIpAddress(request.getRemoteAddr());
        return userService.sendSmsAuthNum(dto);
    }

    @ApiOperation("验证手机验证码")
    @PostMapping("/via/checkPAuthNum")
    public Result<Integer> checkPAuthNum(@RequestBody UserLoginDto dto){
        return userService.checkPAuthNum(dto);
    }

    @ApiOperation("用户注册")
    @PostMapping("/via/registerUser")
    public Result<UserLoginVo> registerUser(@RequestBody UserRegisterDto dto,HttpServletRequest request){
        dto.setFregisterFrom(request.getHeader("source"));
        return userService.registerUser(dto);
    }

    @ApiOperation("忘记密码")
    @PostMapping("/via/forgotPwd")
    public Result<Integer> forgotPwd(@RequestBody UserRegisterDto dto){
        return userService.forgotPwd(dto);
    }

    @ApiOperation("用户认证")
    @PostMapping("/userVerify")
    public Result<Integer> userVerify(@RequestBody UserVerifyDto dto,HttpServletRequest request){
        dto.setFuid(Long.parseLong(request.getHeader("xyid")));
        return userService.userVerify(dto);
    }

    @ApiOperation("用户认证信息查询")
    @PostMapping("/queryUserVerify")
    public Result<UserVerifyVo> queryUserVerify(@RequestBody UserVerifyDto dto,HttpServletRequest request){
        dto.setFuid(Long.parseLong(request.getHeader("xyid")));
        return userService.queryUserVerify(dto);
    }

    @ApiOperation("用户认证信息-类目配置查询")
    @PostMapping("/via/queryCategory")
    public Result<List<VerifyCategoryVo>> queryCategory(HttpServletRequest request){
        return userService.queryCategory();
    }

    @ApiOperation("用户认证信息-平台配置查询")
    @PostMapping("/via/queryPlatform")
    public Result<List<VerifyPlatformVo>> queryPlatform(HttpServletRequest request){
        return userService.queryPlatform();
    }

    @ApiOperation("用户认证状态查询")
    @PostMapping("/queryUserVerifyStatus")
    public Result<UserVo> queryUserVerifyStatus(HttpServletRequest request){
        Long fuid = Long.parseLong(request.getHeader("xyid"));
        return userService.queryUserVerifyStatus(fuid);
    }

    @ApiOperation("账户与安全验证-发送短信/邮件")
    @PostMapping("/accountSecurityVerification")
    public Result<SendSmsVo> accountSecurityVerification(@RequestBody UserSecurityDto dto, HttpServletRequest request){
        dto.setFuid(Long.parseLong(request.getHeader("xyid")));
        dto.setIpAddress(request.getRemoteAddr());
        return userService.accountSecurityVerification(dto);
    }

    @ApiOperation("验证邮箱验证码")
    @PostMapping("/via/checkEmailAuthNum")
    public Result<Integer> checkEmailAuthNum(@RequestBody UserSecurityDto dto){
        return userService.checkEmailAuthNum(dto);
    }

    @ApiOperation("修改/设置支付密码")
    @PostMapping("/modifiyPayPwd")
    public Result<Integer> modifiyPayPwd(@RequestBody UserSecurityDto dto, HttpServletRequest request){
        dto.setFuid(Long.parseLong(request.getHeader("xyid")));
        return userService.modifiyPayPwd(dto);
    }

    @ApiOperation("修改密码")
    @PostMapping("/modifiyPwd")
    public Result<Integer> modifiyPwd(@RequestBody UserSecurityDto dto, HttpServletRequest request){
        dto.setFuid(Long.parseLong(request.getHeader("xyid")));
        return userService.modifiyPwd(dto);
    }

    @ApiOperation("修改邮箱")
    @PostMapping("/modifiyEmailAccount")
    public Result<Integer> modifiyEmailAccount(@RequestBody UserSecurityDto dto, HttpServletRequest request){
        dto.setFuid(Long.parseLong(request.getHeader("xyid")));
        return userService.modifiyEmailAccount(dto);
    }

    @ApiOperation("修改手机号-短信发送")
    @PostMapping("/modifiyMobileSendSMS")
    public Result<SendSmsVo> modifiyMobileSendSMS(@RequestBody UserSecurityDto dto, HttpServletRequest request){
        dto.setFuid(Long.parseLong(request.getHeader("xyid")));
        dto.setIpAddress(request.getRemoteAddr());
        return userService.modifiyMobileSendSMS(dto);
    }

    @ApiOperation("修改手机号")
    @PostMapping("/modifiyMobile")
    public Result<Integer> modifiyMobile(@RequestBody UserSecurityDto dto, HttpServletRequest request){
        dto.setFuid(Long.parseLong(request.getHeader("xyid")));
        return userService.modifiyMobile(dto);
    }

    @ApiOperation("用户信息查询")
    @PostMapping("/queryUserInfo")
    public Result<UserVo> queryUserInfo(HttpServletRequest request){
        Long fuid = Long.parseLong(request.getHeader("xyid"));
        return userService.queryUserInfo(fuid);
    }

    @ApiOperation("认证优惠券弹窗")
    @PostMapping("/queryPopupWindowsStatus")
    public Result<UserVo> queryPopupWindowsStatus(HttpServletRequest request){
        Long fuid = Long.parseLong(request.getHeader("xyid"));
        return userService.queryPopupWindowsStatus(fuid);
    }

    @ApiOperation("修改用户名")
    @PostMapping("/modifiyUserNickname")
    public Result<Integer> modifiyUserNickname(@RequestBody UserDto dto, HttpServletRequest request){
        dto.setFuid(Long.parseLong(request.getHeader("xyid")));
        return userService.modifiyUserNickname(dto);
    }

    @ApiOperation("H5链接登录后领取优惠券")
    @PostMapping("/couponLinkReceive")
    public Result couponLinkReceive(@RequestBody CouponLinkDto dto, HttpServletRequest request){
        dto.setFuid(Long.parseLong(request.getHeader("xyid")));
        return userService.couponLinkReceive(dto);
    }
}
