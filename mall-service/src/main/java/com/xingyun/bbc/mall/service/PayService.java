package com.xingyun.bbc.mall.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.BalancePayDto;
import com.xingyun.bbc.mall.model.dto.RemittancetRechargeDto;
import com.xingyun.bbc.pay.model.dto.ThirdPayDto;


/**
 * @author jianghui
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
public interface PayService {
	
	Result<?> balancePay(BalancePayDto dto,HttpServletRequest request);
	
	Result<?> createThirdPayUrl(ThirdPayDto dto,HttpServletRequest request);
	
	Result<?> createThirdPayCode(ThirdPayDto dto,HttpServletRequest request);
	
	Result<?> newThirdPayResponse(@PathVariable String urlSuffix, HttpServletRequest request, HttpServletResponse response);
	
	Result<?> addBalance(RemittancetRechargeDto dto);
}
