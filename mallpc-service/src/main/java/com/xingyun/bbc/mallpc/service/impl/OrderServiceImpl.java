package com.xingyun.bbc.mallpc.service.impl;

import com.google.common.collect.Lists;
import com.xingyun.bbc.core.order.api.OrderApi;
import com.xingyun.bbc.core.order.api.OrderPaymentApi;
import com.xingyun.bbc.core.order.api.OrderSkuApi;
import com.xingyun.bbc.core.order.enums.OrderPaymentType;
import com.xingyun.bbc.core.order.enums.OrderStatus;
import com.xingyun.bbc.core.order.po.Order;
import com.xingyun.bbc.core.order.po.OrderPayment;
import com.xingyun.bbc.core.order.po.OrderSku;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.supplier.api.SupplierTransportOrderApi;
import com.xingyun.bbc.core.supplier.enums.TradeTypeEnums;
import com.xingyun.bbc.core.supplier.po.SupplierTransportOrder;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.constants.MallPcConstants;
import com.xingyun.bbc.mallpc.common.enums.OrderPaymentStatusEnum;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.DateUtils;
import com.xingyun.bbc.mallpc.common.utils.ResultUtils;
import com.xingyun.bbc.mallpc.model.dto.pay.OrderExportDto;
import com.xingyun.bbc.mallpc.model.vo.pay.*;
import com.xingyun.bbc.mallpc.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2019/12/20 17:37
 * @description: TODO
 * @package com.xingyun.bbc.mallpc.service.impl
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private DozerHolder dozerHolder;

    @Resource
    private OrderPaymentApi orderPaymentApi;

    @Resource
    private OrderApi orderApi;

    @Resource
    private OrderSkuApi orderSkuApi;

    @Resource
    private SupplierTransportOrderApi transportOrderApi;

    @Override
    public List<Object> selectListForExcelExport(Object queryParams, int page) {
        if (!(queryParams instanceof OrderExportDto)) {
            return Lists.newArrayList();
        }
        OrderExportDto orderExportDto = (OrderExportDto) queryParams;
        if (orderExportDto.getForderStatus() == null
                || OrderStatus.WAIT_PAYMENT.getCode().equals(orderExportDto.getForderStatus())) {
            return buildOrderPaymentExport(page, orderExportDto);
        } else {
            return buildOrderExport(page, orderExportDto);
        }
    }

    private List<Object> buildOrderPaymentExport(int page, OrderExportDto orderExportDto) {
        List<OrderPaymentExportVo> exportDataList = new ArrayList<>();
        Criteria<OrderPayment, Object> orderPaymentCriteria = buildOrderPaymentCriteria(orderExportDto);
        if (null == orderPaymentCriteria) {
            return Lists.newArrayList(exportDataList);
        }

        orderPaymentCriteria.page(page, orderExportDto.getPageSize());
        List<OrderPayment> orderPaymentList = ResultUtils.getData(orderPaymentApi.queryByCriteria(orderPaymentCriteria));
        if (CollectionUtils.isEmpty(orderPaymentList)) {
            return Lists.newArrayList(exportDataList);
        }
        for (OrderPayment orderPayment : orderPaymentList) {
            OrderPaymentExportVo orderPaymentExportVo = dozerHolder.convert(orderPayment, OrderPaymentExportVo.class);
            orderPaymentExportVo.setFcreateTime(DateUtils.formatDate(orderPayment.getFcreateTime()));

            OrderPaymentStatusEnum orderPaymentStatusEnum = OrderPaymentStatusEnum.getByStatus(orderPayment.getForderStatus());
            orderPaymentExportVo.setForderStatusStr(orderPaymentStatusEnum == null ? "" : orderPaymentStatusEnum.getStatusDesc());

            orderPaymentExportVo.setFtotalOrderAmountStr(new BigDecimal(orderPayment.getFtotalOrderAmount()).divide(MallPcConstants.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            String payType = OrderPaymentType.getName(orderPayment.getForderPayType());
            orderPaymentExportVo.setForderPayTypeStr(StringUtils.equals(payType, "undefined") ? "null" : payType);

            dealOrder(orderPaymentExportVo, orderPayment.getForderPaymentId());

            if (!Integer.valueOf(2).equals(orderPayment.getForderType())){
                orderPaymentExportVo.setFpayerName("");
                orderPaymentExportVo.setFpayerCardId("");
            }
            exportDataList.add(orderPaymentExportVo);
        }
        log.info("支付单导出数据构造完成");
        return Lists.newArrayList(exportDataList);
    }

    private List<Object> buildOrderExport(int page, OrderExportDto orderExportDto) {
        List<OrderDetailExportVo> exportDataList = new ArrayList<>();
        Criteria<Order, Object> orderCriteria = buildOrderCriteria(orderExportDto);
        if (null == orderCriteria) {
            return Lists.newArrayList(exportDataList);
        }
        orderCriteria.page(page, orderExportDto.getPageSize());
        List<Order> orderList = ResultUtils.getData(orderApi.queryByCriteria(orderCriteria));
        if (CollectionUtils.isEmpty(orderList)) {
            return Lists.newArrayList(exportDataList);
        }
        for (Order order : orderList) {
            OrderExportVo orderExportVo = buildOrderExportVo(order);
            OrderDetailExportVo orderDetailExportVo = dozerHolder.convert(orderExportVo, OrderDetailExportVo.class);
            buildPayInfo(orderDetailExportVo, order.getForderPaymentId());
            exportDataList.add(orderDetailExportVo);
        }
        log.info("销售单导出数据构造完成");
        return Lists.newArrayList(exportDataList);
    }

    private void buildPayInfo(OrderDetailExportVo orderDetailExportVo, String forderPaymentId) {
        OrderPayment orderPayment = ResultUtils.getDataNotNull(orderPaymentApi.queryById(forderPaymentId), MallPcExceptionCode.ORDER_NOT_EXIST);
        BeanUtils.copyProperties(orderPayment, orderDetailExportVo);
        OrderPaymentStatusEnum orderPaymentStatusEnum = OrderPaymentStatusEnum.getByStatus(orderPayment.getForderStatus());
        orderDetailExportVo.setForderPaymentStatusStr(orderPaymentStatusEnum == null ? "" : orderPaymentStatusEnum.getStatusDesc());
        orderDetailExportVo.setFcreateTime(DateUtils.formatDate(orderPayment.getFcreateTime()));
        if (!Integer.valueOf(2).equals(orderPayment.getForderType())){
            orderDetailExportVo.setFpayerName("");
            orderDetailExportVo.setFpayerCardId("");
        }
    }

    private void dealOrder(OrderPaymentExportVo orderPaymentExportVo, String forderPaymentId) {
        List<OrderExportVo> orderExportVoList = new ArrayList<>();
        Order query = new Order();
        query.setForderPaymentId(forderPaymentId);
        List<Order> orderList = ResultUtils.getDataNotNull(orderApi.queryList(query), MallPcExceptionCode.ORDER_NOT_EXIST);
        for (Order order : orderList) {
            orderExportVoList.add(buildOrderExportVo(order));
        }
        orderPaymentExportVo.setOrderExportVoList(orderExportVoList);
    }

    private OrderExportVo buildOrderExportVo(Order order) {
        OrderExportVo orderExportVo = dozerHolder.convert(order, OrderExportVo.class);

        String orderStatus = OrderStatus.getName(order.getForderStatus());
        orderExportVo.setForderStatusStr(StringUtils.equals(orderStatus, "undefined") ? "null" : orderStatus);

        orderExportVo.setFfreightAmountStr(new BigDecimal(order.getFfreightAmount()).divide(MallPcConstants.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        orderExportVo.setFtaxAmountStr(new BigDecimal(order.getFtaxAmount()).divide(MallPcConstants.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        orderExportVo.setForderDiscountAmountStr(new BigDecimal(order.getForderDiscountAmount()).divide(MallPcConstants.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        orderExportVo.setForderAmountStr(new BigDecimal(order.getForderAmount()).divide(MallPcConstants.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        Long fagentAmount = Integer.valueOf(2).equals(order.getForderType()) ? order.getForderAmount() : 0L;
        String fagentAmountStr = new BigDecimal(fagentAmount).divide(MallPcConstants.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        orderExportVo.setFagentAmountStr(fagentAmountStr);
        orderExportVo.setFtaxDifferenceStr(new BigDecimal(order.getFtaxDifference()).divide(MallPcConstants.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        orderExportVo.setFbuyAgentIncomeStr(new BigDecimal(order.getFbuyAgentIncome()).divide(MallPcConstants.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

        dealOrderSku(orderExportVo, order.getForderId());
        dealOrderExpress(orderExportVo, order.getForderId());
        return orderExportVo;
    }

    private void dealOrderSku(OrderExportVo orderExportVo, String forderId) {
        List<OrderSkuExportVo> orderSkuExportVoList = new ArrayList<>();
        OrderSku query = new OrderSku();
        query.setForderId(forderId);
        List<OrderSku> orderList = ResultUtils.getDataNotNull(orderSkuApi.queryList(query), MallPcExceptionCode.ORDER_NOT_EXIST);
        for (OrderSku orderSku : orderList) {
            OrderSkuExportVo orderSkuExportVo = dozerHolder.convert(orderSku, OrderSkuExportVo.class);

            orderSkuExportVo.setFtradeTypeStr(TradeTypeEnums.getTradeType(orderSku.getFtradeType().toString()));

            orderSkuExportVo.setFskuPriceStr(new BigDecimal(orderSku.getFskuPrice()).divide(MallPcConstants.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            orderSkuExportVo.setFskuAmountStr(new BigDecimal(orderSku.getFskuAmount()).divide(MallPcConstants.ONE_HUNDRED).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            orderSkuExportVoList.add(orderSkuExportVo);
        }
        orderExportVo.setOrderSkuExportVoList(orderSkuExportVoList);
    }

    private void dealOrderExpress(OrderExportVo orderExportVo, String forderId) {
        List<OrderExpressExportVo> orderExpressExportVoList = new ArrayList<>();
        SupplierTransportOrder query = new SupplierTransportOrder();
        query.setForderId(forderId);
        List<SupplierTransportOrder> transportOrderList = ResultUtils.getData(transportOrderApi.queryList(query));
        if (CollectionUtils.isNotEmpty(transportOrderList)) {
            for (SupplierTransportOrder supplierTransportOrder : transportOrderList) {
                OrderExpressExportVo orderExpressExportVo = new OrderExpressExportVo();
                orderExpressExportVo.setFcompanyName(supplierTransportOrder.getFshippingName());
                orderExpressExportVo.setFexpressBillNo(supplierTransportOrder.getForderLogisticsNo());
                orderExpressExportVoList.add(orderExpressExportVo);
            }
        }
        orderExportVo.setOrderExpressExportVoList(orderExpressExportVoList);
    }

    private Criteria<OrderPayment, Object> buildOrderPaymentCriteria(OrderExportDto orderExportDto) {
        Criteria<OrderPayment, Object> orderPaymentCriteria = Criteria.of(OrderPayment.class);
        if (CollectionUtils.isNotEmpty(orderExportDto.getOrderPaymentIdList())) {
            //支付订单id集合精确查询
            orderPaymentCriteria.andIn(OrderPayment::getForderPaymentId, orderExportDto.getOrderPaymentIdList());
            return orderPaymentCriteria;
        }
        List<String> forderPaymenIdList = new ArrayList<>();
        List<String> forderIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(orderExportDto.getFskuName()) || StringUtils.isNotBlank(orderExportDto.getFskuCode())) {
            Criteria<OrderSku, Object> orderSkuCriteria = Criteria.of(OrderSku.class).fields(OrderSku::getForderId);
            if (StringUtils.isNotBlank(orderExportDto.getFskuName())) {
                orderSkuCriteria.andLike(OrderSku::getFskuName, orderExportDto.getFskuName() + "%");
            }
            if (StringUtils.isNotBlank(orderExportDto.getFskuCode())) {
                orderSkuCriteria.andLike(OrderSku::getFskuCode, orderExportDto.getFskuCode() + "%");
            }
            List<OrderSku> orderSkuList = ResultUtils.getData(orderSkuApi.queryByCriteria(orderSkuCriteria));
            if (CollectionUtils.isEmpty(orderSkuList)) {
                //根据商品名称或商品编码没有查到订单商品数据
                return null;
            }
            forderIdList.addAll(orderSkuList.stream().map(OrderSku::getForderId).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(forderIdList) || StringUtils.isNotEmpty(orderExportDto.getForderId())) {
            Criteria<Order, Object> orderCriteria = Criteria.of(Order.class).fields(Order::getForderPaymentId);
            if (CollectionUtils.isNotEmpty(forderIdList)) {
                orderCriteria.andIn(Order::getForderId, forderIdList);
            }
            if (StringUtils.isNotEmpty(orderExportDto.getForderId())) {
                orderCriteria.andEqualTo(Order::getForderId, orderExportDto.getForderId());
            }
            List<Order> orderList = ResultUtils.getData(orderApi.queryByCriteria(orderCriteria));
            if (CollectionUtils.isEmpty(orderList)) {
                //没有查到订单数据
                return null;
            }
            forderPaymenIdList.addAll(orderList.stream().map(Order::getForderPaymentId).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(forderPaymenIdList)) {
            orderPaymentCriteria.andIn(OrderPayment::getForderPaymentId, forderPaymenIdList);
        }
        if (StringUtils.isNotEmpty(orderExportDto.getForderPaymentId())) {
            orderPaymentCriteria.andEqualTo(OrderPayment::getForderPaymentId, orderExportDto.getForderPaymentId());
        }
        if (StringUtils.isNotEmpty(orderExportDto.getFdeliveryName())) {
            orderPaymentCriteria.andLike(OrderPayment::getFdeliveryName, orderExportDto.getFdeliveryName() + "%");
        }
        if (StringUtils.isNotEmpty(orderExportDto.getFdeliveryMobile())) {
            orderPaymentCriteria.andEqualTo(OrderPayment::getFdeliveryMobile, orderExportDto.getFdeliveryMobile());
        }
        if (null != orderExportDto.getForderStatus()) {
            orderPaymentCriteria.andEqualTo(OrderPayment::getForderStatus, orderExportDto.getForderStatus());
        }
        if (StringUtils.isNotEmpty(orderExportDto.getForderTimeStart())) {
            orderPaymentCriteria.andGreaterThanOrEqualTo(OrderPayment::getFcreateTime, DateUtils.parseDate(orderExportDto.getForderTimeStart()));
        }
        if (StringUtils.isNotEmpty(orderExportDto.getForderTimeEnd())) {
            orderPaymentCriteria.andLessThanOrEqualTo(OrderPayment::getFcreateTime, DateUtils.parseDate(orderExportDto.getForderTimeEnd()));
        }
        orderPaymentCriteria.andEqualTo(OrderPayment::getFuid, orderExportDto.getFuid());
        return orderPaymentCriteria;
    }

    private Criteria<Order, Object> buildOrderCriteria(OrderExportDto orderExportDto) {
        Criteria<Order, Object> orderCriteria = Criteria.of(Order.class);
        if (CollectionUtils.isNotEmpty(orderExportDto.getOrderIdList())) {
            //订单id集合精确查询
            orderCriteria.andIn(Order::getForderId, orderExportDto.getOrderIdList());
            return orderCriteria;
        }
        List<String> forderIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(orderExportDto.getFskuName()) || StringUtils.isNotBlank(orderExportDto.getFskuCode())) {
            Criteria<OrderSku, Object> orderSkuCriteria = Criteria.of(OrderSku.class).fields(OrderSku::getForderId);
            if (StringUtils.isNotBlank(orderExportDto.getFskuName())) {
                orderSkuCriteria.andLike(OrderSku::getFskuName, orderExportDto.getFskuName() + "%");
            }
            if (StringUtils.isNotBlank(orderExportDto.getFskuCode())) {
                orderSkuCriteria.andLike(OrderSku::getFskuCode, orderExportDto.getFskuCode() + "%");
            }
            List<OrderSku> orderSkuList = ResultUtils.getData(orderSkuApi.queryByCriteria(orderSkuCriteria));
            if (CollectionUtils.isEmpty(orderSkuList)) {
                //根据商品名称或商品编码没有查到订单商品数据
                return null;
            }
            forderIdList.addAll(orderSkuList.stream().map(OrderSku::getForderId).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(forderIdList)) {
            orderCriteria.andIn(Order::getForderId, forderIdList);
        }
        if (StringUtils.isNotEmpty(orderExportDto.getForderId())) {
            orderCriteria.andEqualTo(Order::getForderId, orderExportDto.getForderId());
        }
        if (null != orderExportDto.getForderStatus()) {
            if (orderExportDto.getForderStatus().equals(2) || orderExportDto.getForderStatus().equals(3) || orderExportDto.getForderStatus().equals(4)){
                List<Integer> orderStatusList = new ArrayList<>();
                orderStatusList.add(2);
                orderStatusList.add(3);
                orderStatusList.add(4);
                orderCriteria.andIn(Order::getForderStatus,orderStatusList);
            }else {
                orderCriteria.andEqualTo(Order::getForderStatus, orderExportDto.getForderStatus());
            }
        }
        if (StringUtils.isNotEmpty(orderExportDto.getForderTimeStart())) {
            orderCriteria.andGreaterThanOrEqualTo(Order::getFcreateTime, DateUtils.parseDate(orderExportDto.getForderTimeStart()));
        }
        if (StringUtils.isNotEmpty(orderExportDto.getForderTimeEnd())) {
            orderCriteria.andLessThanOrEqualTo(Order::getFcreateTime, DateUtils.parseDate(orderExportDto.getForderTimeEnd()));
        }

        Criteria<OrderPayment, Object> orderPaymentCriteria = Criteria.of(OrderPayment.class).fields(OrderPayment::getForderPaymentId);
        orderPaymentCriteria.andEqualTo(OrderPayment::getFuid, orderExportDto.getFuid());
        if (StringUtils.isNotEmpty(orderExportDto.getForderPaymentId())) {
            orderPaymentCriteria.andEqualTo(OrderPayment::getForderPaymentId, orderExportDto.getForderPaymentId());
        }
        if (StringUtils.isNotEmpty(orderExportDto.getFdeliveryName())) {
            orderPaymentCriteria.andLike(OrderPayment::getFdeliveryName, orderExportDto.getFdeliveryName() + "%");
        }
        if (StringUtils.isNotEmpty(orderExportDto.getFdeliveryMobile())) {
            orderPaymentCriteria.andEqualTo(OrderPayment::getFdeliveryMobile, orderExportDto.getFdeliveryMobile());
        }
        List<OrderPayment> orderPaymentList = ResultUtils.getData(orderPaymentApi.queryByCriteria(orderPaymentCriteria));
        if (CollectionUtils.isEmpty(orderPaymentList)) {
            //没有查到支付订单商品数据
            return null;
        }
        orderCriteria.andIn(Order::getForderPaymentId, orderPaymentList.stream().map(OrderPayment::getForderPaymentId).collect(Collectors.toList()));
        return orderCriteria;
    }
}