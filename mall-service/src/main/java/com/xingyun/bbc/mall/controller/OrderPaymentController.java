package com.xingyun.bbc.mall.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.service.GoodDetailService;
import com.xingyun.bbc.order.api.OrderPaymentCenterApi;
import com.xingyun.bbc.order.model.dto.order.OrderCanelDto;
import com.xingyun.bbc.order.model.dto.order.OrderDetailDto;
import com.xingyun.bbc.order.model.dto.order.OrderPaymentDto;
import com.xingyun.bbc.order.model.vo.order.OrderCancelVo;
import com.xingyun.bbc.order.model.vo.order.OrderDetailVo;
import com.xingyun.bbc.order.model.vo.order.OrderPaymentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Administrator
 * @version V1.0
 * @Title:
 * @Package com.xingyun.bbc.mall.controller
 * @Description: (用一句话描述该文件做什么)
 * @date 2019/9/7 10:50
 */
@RestController
@RequestMapping("/orderPayment")
@Api("订单查询")
public class OrderPaymentController {

	public static final Logger logger = LoggerFactory.getLogger(GoodDetailService.class);

	@Autowired
	OrderPaymentCenterApi orderPaymentApi;

	@ApiOperation("查询订单列表")
	@PostMapping("/selectOrderList")
	public Result<OrderPaymentVo> selectOrderList(@RequestBody OrderPaymentDto orderPaymentDto, HttpServletRequest request) {
		logger.info("查询订单列表" + orderPaymentDto.toString());
		Long fuid = Long.parseLong(request.getHeader("xyid"));
		orderPaymentDto.setFuid(fuid);
		logger.info("查询订单列表" + orderPaymentApi.selectOrderList(orderPaymentDto).toString());
		return orderPaymentApi.selectOrderList(orderPaymentDto);
	}

	@ApiOperation("查询订单详情")
	@PostMapping("/selectOrderDetail")
	public Result<OrderDetailVo> selectOrderDetail(@RequestBody OrderDetailDto orderDetailDto) {
		return orderPaymentApi.selectOrderDetail(orderDetailDto);
	}

	@ApiOperation("取消订单")
	@PostMapping("/cancelOrder")
	public Result<OrderCancelVo> cancelOrder(@RequestBody OrderCanelDto orderCanelDto) {
		return orderPaymentApi.cancelOrder(orderCanelDto);
	}
}
