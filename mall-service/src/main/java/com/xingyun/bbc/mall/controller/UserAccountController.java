package com.xingyun.bbc.mall.controller;

import com.google.common.base.Strings;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.dto.AccountWithdrawDto;
import com.xingyun.bbc.mall.model.dto.UserRechargeDto;
import com.xingyun.bbc.mall.service.UserAccountService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jianghui
 * @Description: 用户账户明细
 * @createTime: 2019-09-03 11:00
 */
@RestController
@RequestMapping(value = "/userAccount")
public class UserAccountController {
    @Autowired
    private UserAccountService userAccountService;
    
    @Autowired
    private UserApi userApi;
    

	@ApiOperation("账户余额充值")
	@PostMapping("/userRecharge")
	public Result<?> userRecharge(@RequestBody @ApiParam(name = "充值金额", value = "传入json格式", required = false) UserRechargeDto userRechargeDto, HttpServletRequest request){
    	//获取用户id
		Long fuid = Long.valueOf(request.getHeader("xyid"));
		if (fuid == null || "".equals(fuid)) {
			return Result.failure(MallExceptionCode.NO_USER);
		}
		//对充值金额frecharge进行判断
		if(Strings.isNullOrEmpty(userRechargeDto.getFrecharger())){
			return Result.failure(MallExceptionCode.PARAM_ERROR);
		}
		userRechargeDto.setFuid(Long.valueOf(fuid));
		return userAccountService.insertUserBalance(userRechargeDto);
	}
	
	@ApiOperation("账户提现")
	@PostMapping("/accountWithdraw")
	public Result<?> accountWithdraw(@RequestBody @ApiParam(name = "账号提现", value = "传入json格式", required = false) AccountWithdrawDto accountWithdrawDto, HttpServletRequest request) {
		//获取用户id
		String fuid = request.getHeader("xyid");
		if (fuid == null || "".equals(fuid)) {
			return Result.failure(MallExceptionCode.NO_USER);
		}
		//验证该用户是否符合条件
        Result<User> user = userApi.queryById(fuid);
		if(user.getData().getFfreezeStatus() == 2 ||user.getData().getFfreezeStatus() == 3){
			return Result.failure(MallExceptionCode.USER_FREEZE_ERROR);
		}
		//验证该用户是否设置交易密码
		if (Strings.isNullOrEmpty(user.getData().getFwithdrawPasswd())) {
			return Result.failure(MallExceptionCode.NULL_ERROR_WITHDRAWPSD);
		}
		//验证加密后的账户密码
//		if (!"1".equals(userAccountService.checkWithdrawPwd(Integer.parseInt(fuid), EncryptUtils.aesDecrypt(userAccountTrans.getFwithdrawPasswd())))){
//			return XyResonseEntity.badReq(Xyb2bRetCodeConstant.WITHDRAW_PSD_WRONG);
//		}
		//设置用户提现状态
		accountWithdrawDto.setFuid(Long.valueOf(fuid));
		accountWithdrawDto.setFtransTypes(2);//状态为提现申请
		accountWithdrawDto.setFtransReason("提现申请");
		//将数据插入提现订单表
		return userAccountService.insertWithdraw(accountWithdrawDto);

	}
  
}
