package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.user.ResetPasswordDto;
import com.xingyun.bbc.mallpc.model.dto.user.SendSmsCodeDto;
import com.xingyun.bbc.mallpc.model.dto.user.UserLoginDto;
import com.xingyun.bbc.mallpc.model.dto.user.UserRegisterDto;
import com.xingyun.bbc.mallpc.model.vo.coupon.MyCouponVo;
import com.xingyun.bbc.mallpc.model.vo.user.SendSmsCodeVo;
import com.xingyun.bbc.mallpc.model.vo.user.UserLoginVo;
import com.xingyun.bbc.mallpc.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-18
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@RestController
@Api("用户登录注册")
@RequestMapping("/user")
public class UserController {


    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation("获取注册页图片")
    @GetMapping("/via/viewlogin")
    public Result<String> guideLogin() {
        return userService.guideLogin();
    }

    @ApiOperation("用户登录")
    @PostMapping("/via/userLogin")
    public Result<UserLoginVo> userLogin(@Validated @RequestBody UserLoginDto userLoginDto) {
        return userService.userLogin(userLoginDto);
    }

    @ApiOperation("用户注册")
    @PostMapping("/via/register")
    public Result<UserLoginVo> register(@Validated @RequestBody UserRegisterDto userRegisterDto) {
        return userService.register(userRegisterDto);
    }

    @ApiOperation("发送验证码")
    @PostMapping("/via/sendSmsCode")
    public Result<SendSmsCodeVo> sendSmsCode(@Validated @RequestBody SendSmsCodeDto sendSmsCodeDto) {
        return userService.sendSmsCode(sendSmsCodeDto);
    }

    @ApiOperation("重置密码")
    @PostMapping("/via/resetPwd")
    public Result resetPwd(@Validated @RequestBody ResetPasswordDto resetPasswordDto) {
        return userService.resetPwd(resetPasswordDto);
    }
    
    @ApiOperation("修改/设置支付密码")
    @PostMapping("/via/modifiyPayPwd")
    public Result modifiyPayPwd(@Validated @RequestBody ResetPasswordDto resetPasswordDto) {
        return userService.modifiyPayPwd(resetPasswordDto);
    }

    @ApiOperation("查询新人注册优惠券")
    @GetMapping("/queryRegisterCoupon")
    public Result<MyCouponVo> queryRegisterCoupon() {
        return userService.queryRegisterCoupon();
    }

    @ApiOperation("查询登录信息")
    @GetMapping("/queryLoginInfo")
    public Result<UserLoginVo> queryLoginInfo(){
        return userService.queryLoginInfo();
    }
}
