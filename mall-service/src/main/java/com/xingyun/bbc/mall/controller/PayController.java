package com.xingyun.bbc.mall.controller;


import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.enums.CompanyBankInfoEnums;
import com.xingyun.bbc.mall.model.dto.BalancePayDto;
import com.xingyun.bbc.mall.model.dto.RemittancetRechargeDto;
import com.xingyun.bbc.mall.service.PayService;
import com.xingyun.bbc.pay.model.dto.ThirdPayDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jianghui
 * @version V1.0
 * @Title:
 * @Package com.xingyun.bbc.mall.controller
 * @Description: (用一句话描述该文件做什么)
 * @date 2019/9/5 18:58
 */
@Api("支付订单")
@RestController
@RequestMapping("/pay")
public class PayController {

	@Autowired
	private PayService payService;

	@ApiOperation("余额支付")
	@PostMapping(value = "/balancePay")
	public Result<?> balancePay(@RequestBody BalancePayDto dto, HttpServletRequest request) {
		return payService.balancePay(dto, request);
	}

	@ApiOperation("生成第三方支付链接")
	@PostMapping("/createThirdPayUrl")
	public Result<?> createThirdPayUrl(@RequestBody ThirdPayDto dto, HttpServletRequest request) {
		return payService.createThirdPayUrl(dto, request);
	}

	@ApiOperation("生成第三方支付二维码")
	@PostMapping("/createThirdPayCode")
	public Result<?> createThirdPayCode(@RequestBody ThirdPayDto dto, HttpServletRequest request) {
		return payService.createThirdPayCode(dto, request);
	}

	@RequestMapping("/via/thirdpayresponse_{urlSuffix}")
	public Result<?> thirdPayResponse(@PathVariable String urlSuffix, HttpServletRequest request,HttpServletResponse response) {
		return payService.thirdPayResponse(urlSuffix, request, response);
	}

	

	@ApiOperation("用户线下汇款获取公司银行账号信息")
	@PostMapping("/getCompanyBankInfo")
	public Result<?> getCompanyBankInfo() 
	{
		Map<String, String> result=new HashMap<String,String>();
		result.put("companyName", CompanyBankInfoEnums.offlinePay.COMPANYNAME.getDesc());
		result.put("companyBankCard", CompanyBankInfoEnums.offlinePay.COMPANYBANKCARD.getDesc());
		result.put("companyBankName",CompanyBankInfoEnums.offlinePay.COMPANYBANKNAME.getDesc());
		return Result.success(result);
	}
	
	@ApiOperation("用户线下汇款余额充值申请")
	@PostMapping( "/addBalance")
	public Result<?> addBalance(@RequestBody RemittancetRechargeDto dto) 
	{
		return payService.addBalance(dto);
	}
	
}
