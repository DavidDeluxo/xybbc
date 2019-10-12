package com.xingyun.bbc.mall.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.order.api.OrderSettleSplitApi;
import com.xingyun.bbc.order.model.dto.order.*;
import com.xingyun.bbc.order.model.vo.order.OrderSetBuyAgentVo;
import com.xingyun.bbc.order.model.vo.order.OrderSettleVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Thstone
 * @version V1.0
 * @Title:
 * @Package com.xingyun.bbc.mall.controller
 * @Description: (用一句话描述该文件做什么)
 * @date 2019/9/5 16:32
 */
@RestController
@RequestMapping("/orderSettleSplit")
@Api("结算")
public class OrderSettleController {

	@Autowired
	OrderSettleSplitApi orderSettleSplitApi;

	@ApiOperation("发起结算")
	@PostMapping("/launchSettle")
	public Result<OrderSettleVo> launchSettle(@RequestBody  OrderSettleDto orderSettleDto,HttpServletRequest request) {
		Long fuid = Long.valueOf(request.getHeader("xyid"));
		orderSettleDto.setFuid(fuid);
		return orderSettleSplitApi.launchSettle(orderSettleDto);
	}

	@ApiOperation("查看拆单之后待结算的订单")
	@PostMapping("/querySettle")
	public Result<OrderSettleVo> querySettle(@RequestBody OrderSubmitDto orderSubmitDto, HttpServletRequest request) {
		Long fuid = Long.valueOf(request.getHeader("xyid"));
		orderSubmitDto.setFuid(fuid);
		return orderSettleSplitApi.querySettle(orderSubmitDto);
	}

	@ApiOperation("设置代购价(单个)")
	@RequestMapping(value ="/updateBuyAgent",method = RequestMethod.POST)
	public Result<OrderSetBuyAgentVo> updateBuyAgent(@RequestBody  OrderSettleSplitDto orderSettleSplitDto){
		return orderSettleSplitApi.updateBuyAgent(orderSettleSplitDto);
	}

	@ApiOperation("设置代购价(批量)")
	@RequestMapping(value ="/updateBuyAgentBatch",method = RequestMethod.POST)
	public Result<List<OrderSetBuyAgentVo>> updateBuyAgentBatch(@RequestBody  OrderBuyAgentDto orderBuyAgentDto){
		return orderSettleSplitApi.updateBuyAgentBatch(orderBuyAgentDto);
	}

	@ApiOperation("立即购买")
	@PostMapping("/immediatePurchase")
	public Result<OrderSettleVo> immediatePurchase(@RequestBody @Validated OrderPurchaseDto orderPurchaseDto,  HttpServletRequest request) {
		Long fuid = Long.valueOf(request.getHeader("xyid"));
		orderPurchaseDto.setFuid(fuid);
		return orderSettleSplitApi.immediatePurchase(orderPurchaseDto);
	}
}
