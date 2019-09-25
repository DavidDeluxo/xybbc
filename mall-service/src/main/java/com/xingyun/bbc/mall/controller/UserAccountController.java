package com.xingyun.bbc.mall.controller;

import com.google.common.base.Strings;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
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

}
