package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.utils.RequestHolder;
import com.xingyun.bbc.order.api.OrderCenterApi;
import com.xingyun.bbc.order.api.TransportOrderCenterApi;
import com.xingyun.bbc.order.api.UserDeliveryCenterApi;
import com.xingyun.bbc.order.model.dto.order.*;
import com.xingyun.bbc.order.model.vo.order.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lchm
 * @version V1.0
 * @Title:
 * @Package com.xingyun.bbc.mall.controller
 * @Description: (用一句话描述该文件做什么)
 * @date 2019/9/5 18:58
 */
@Api("订单")
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderCenterApi orderApi;

    @Resource
    private TransportOrderCenterApi transportOrderCenterApi;

    @Resource
    private UserDeliveryCenterApi userDeliveryCenterApi;

    @ApiOperation("提交订单")
    @PostMapping("/submit")
    public Result<OrderSubmitVo> submit(@RequestBody OrderSubmitDto orderSubmitDto, HttpServletRequest request) {
        Long fuid = RequestHolder.getUserId();
        String source = request.getHeader("source");
        orderSubmitDto.setFuid(fuid);
        orderSubmitDto.setSource(source);
        return orderApi.submit(orderSubmitDto);
    }

    @ApiOperation("查询发货单物流信息")
    @PostMapping("/queryExpress")
    public Result<ExpressVo> queryExpress(@RequestBody @Validated TransportOrderDto transportOrderDto) {
        return transportOrderCenterApi.queryExpress(transportOrderDto);
    }

    @ApiOperation("查询商品订单下所有发货单的物流信息")
    @PostMapping("/queryExpressBatch")
    public Result<List<ExpressVo>> queryExpressBatch(@RequestBody @Validated OrderExpressDto orderExpressDto) {
        return transportOrderCenterApi.queryExpressBatch(orderExpressDto);
    }

    @ApiOperation("查询订单状态数量信息")
    @PostMapping("/queryOrderStatusCount")
    public Result<List<OrderStatusVo>> queryOrderStatusCount(HttpServletRequest request) {
        Long fuid = RequestHolder.getUserId();
        OrderStatusDto orderStatusDto = new OrderStatusDto();
        orderStatusDto.setFuid(fuid);
        return orderApi.queryOrderStatusCount(orderStatusDto);
    }

    @ApiOperation("确认收货")
    @PostMapping("/confirmReceipt")
    public Result<OrderConfirmVo> confirmReceipt(@RequestBody @Validated OrderConfirmDto orderConfirmDto) {
        return orderApi.updateOrderConfirm(orderConfirmDto);
    }

    @ApiOperation("查询用户默认收货地址")
    @PostMapping("/queryDefaultAddress")
    public Result<AddressVo> confirmReceipt(HttpServletRequest request) {
        Long fuid = RequestHolder.getUserId();
        ShipAddressDto shipAddressDto = new ShipAddressDto();
        shipAddressDto.setFuid(fuid);
        return userDeliveryCenterApi.queryDefaultAddress(shipAddressDto);
    }

    @ApiOperation("发货提醒")
    @PostMapping("/remindDelivery")
    public Result<Void> remindDelivery(@RequestBody @Validated OnlyOrderIdDto orderIdDto) {
        //异步处理提醒，直接返回前端提醒成功
        orderApi.remindDelivery(orderIdDto);
        return Result.success();
    }
}
