package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.service.PayService;
import com.xingyun.bbc.pay.model.dto.ThirdPayDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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

}
