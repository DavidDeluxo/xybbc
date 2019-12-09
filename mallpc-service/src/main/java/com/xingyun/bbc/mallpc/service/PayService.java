package com.xingyun.bbc.mallpc.service;

import javax.servlet.http.HttpServletRequest;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.pay.BalancePayDto;
import com.xingyun.bbc.mallpc.model.dto.pay.CheckPayDto;
import com.xingyun.bbc.order.model.dto.order.OrderPaymentInfoDto;
import com.xingyun.bbc.order.model.vo.order.OrderPaymentInfoVo;
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
	
	Result<?> checkOrderIsPaySuccess(CheckPayDto dto,HttpServletRequest request);
	
	Result<?> checkRechargeIsPaySuccess(CheckPayDto dto,HttpServletRequest request);

	/**
	 * 查询支付信息
	 * @param orderPaymentInfoDto
	 * @return
	 */
	Result<OrderPaymentInfoVo> getPaymentInfo(OrderPaymentInfoDto orderPaymentInfoDto);
}
