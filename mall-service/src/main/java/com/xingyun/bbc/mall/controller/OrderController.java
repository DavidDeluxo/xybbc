package com.xingyun.bbc.mall.controller;

import com.alibaba.fastjson.JSONObject;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.order.api.OrderCenterApi;
import com.xingyun.bbc.order.api.TransportOrderCenterApi;
import com.xingyun.bbc.order.model.dto.order.OrderSubmitDto;
import com.xingyun.bbc.order.model.dto.order.TransportOrderDto;
import com.xingyun.bbc.order.model.vo.order.OrderSubmitVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Thstone
 * @version V1.0
 * @Title:
 * @Package com.xingyun.bbc.mall.controller
 * @Description: (用一句话描述该文件做什么)
 * @date 2019/9/5 18:58
 */
@Api("提交订单")
@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	OrderCenterApi orderApi;
	@Autowired
	TransportOrderCenterApi transportOrderCenterApi;

	@ApiOperation("提交订单")
	@PostMapping("/submit")
	public Result<OrderSubmitVo> submit(@RequestBody  OrderSubmitDto orderSubmitDto, HttpServletRequest request) {
		Long fuid = Long.valueOf(request.getHeader("xyid"));
		String source = request.getHeader("source");
		orderSubmitDto.setFuid(fuid);
		orderSubmitDto.setSource(source);
		return orderApi.submit(orderSubmitDto);
	}

    @ApiOperation("查询发货单物流信息")
    @PostMapping("/queryExpress")
    public Result<JSONObject> queryExpress(@RequestBody  @Validated TransportOrderDto transportOrderDto) {
        return transportOrderCenterApi.queryExpress(transportOrderDto);
    }
}
