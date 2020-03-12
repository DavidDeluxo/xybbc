package com.xingyun.bbc.mall.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.express.model.vo.ExpressBillDetailVo;
import com.xingyun.bbc.express.model.vo.ExpressBillVo;
import com.xingyun.bbc.mall.common.enums.ExpressContextEnum;
import com.xingyun.bbc.mall.model.dto.ExpressDto;
import com.xingyun.bbc.order.api.OrderCenterApi;
import com.xingyun.bbc.order.api.OrderPaymentCenterApi;
import com.xingyun.bbc.order.api.TransportOrderCenterApi;
import com.xingyun.bbc.order.api.UserDeliveryCenterApi;
import com.xingyun.bbc.order.model.dto.order.*;
import com.xingyun.bbc.order.model.vo.order.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

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

    public static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    OrderCenterApi orderApi;
    @Autowired
    TransportOrderCenterApi transportOrderCenterApi;
    @Autowired
    UserDeliveryCenterApi userDeliveryCenterApi;
    @Autowired
    OrderPaymentCenterApi orderPaymentCenterApi;

    private static final String ACCESS_TOKEN = "accessToken";

    @Value("${old.orderList.url}")
    private String old_orderList_URL;

    @ApiOperation("提交订单")
    @PostMapping("/submit")
    public Result<OrderSubmitVo> submit(@RequestBody OrderSubmitDto orderSubmitDto, HttpServletRequest request) {
        Long fuid = Long.valueOf(request.getHeader("xyid"));
        String source = request.getHeader("source");
        orderSubmitDto.setFuid(fuid);
        orderSubmitDto.setSource(source);
        return orderApi.submit(orderSubmitDto);
    }

    @ApiOperation("查询发货单物流信息")
    @PostMapping("/queryExpress")
    public Result<ExpressVo> queryExpress(@RequestBody ExpressDto expressDto) {
        //通过订单号查询需拼接的订单流转信息
        ExpressVo expressVo = new ExpressVo();
        List<TransportSkuVo> transportSkuVoList = new ArrayList<>();
        expressVo.setTransportSkuVoList(transportSkuVoList);
        ExpressStatusTimeVo expressStatusTimeVo = new ExpressStatusTimeVo();
        if(!expressDto.getFtransportOrderId().equals("")){
            TransportOrderDto transportOrderDto = new TransportOrderDto();
            transportOrderDto.setFtransportOrderId(expressDto.getFtransportOrderId());
            Result<ExpressVo> expressVoResult = transportOrderCenterApi.queryExpress(transportOrderDto);
            if (expressVoResult.isSuccess()) {
                expressVo = expressVoResult.getData();
            }
            //根据发货单号反查订单号,后面可能会存在只有发货单号一个传参，需要用发货单反查销售订单号
            //仓库处理中对应发货单创建时间、出库时间对应发货时间
            Result<ExpressStatusTimeVo> result = transportOrderCenterApi.queryExpressStatusTime(transportOrderDto);
            if (result.isSuccess()) {
                if(result.getData() != null){
                    expressStatusTimeVo = result.getData();
                    expressDto.setForderId(expressStatusTimeVo.getForderId());
                }
            }
        }
        if(!expressDto.getForderId().equals("")){
            expressVo = queryOrderStatusTime(expressDto,expressVo,expressStatusTimeVo);
        }
        return Result.success(expressVo);
    }

    private ExpressVo queryOrderStatusTime(ExpressDto expressDto, ExpressVo expressVo, ExpressStatusTimeVo expressStatusTimeVo) {
        List<ExpressBillDetailVo> data = new ArrayList<>();
        ExpressBillVo expressBillVo = new ExpressBillVo();
        //查询订单确认和推送时间
        OnlyOrderIdDto onlyOrderIdDto = new OnlyOrderIdDto();
        onlyOrderIdDto.setForderId(expressDto.getForderId());
        Result<OrderStatusTimeVo> orderStatusTimeVoResult = orderPaymentCenterApi.selectOrderStatusTime(onlyOrderIdDto);
        if(orderStatusTimeVoResult.isSuccess()){
            OrderStatusTimeVo orderStatusTimeVo = orderStatusTimeVoResult.getData();
            if(expressVo.getExpressData() != null){
                data = expressVo.getExpressData().getData();
                expressBillVo = expressVo.getExpressData();
            }
            if(expressStatusTimeVo != null){
                if(expressStatusTimeVo.getFdeliveryTime() != null && !expressStatusTimeVo.getFdeliveryTime().equals("1970-01-01 00:00:00")){
                    ExpressBillDetailVo expressBillDetailVo = new ExpressBillDetailVo();
                    expressBillDetailVo.setFtime(expressStatusTimeVo.getFdeliveryTime());
                    expressBillDetailVo.setContext(ExpressContextEnum.THE_PARCEL_HAS_BEEN_DELIVERED_FROM_THE_WAREHOUSE.getDesc());
                    data.add(expressBillDetailVo);
                }
                if(expressStatusTimeVo.getFcreateTime() != null && !expressStatusTimeVo.getFcreateTime().equals("1970-01-01 00:00:00")){
                    ExpressBillDetailVo expressBillDetailVo = new ExpressBillDetailVo();
                    expressBillDetailVo.setFtime(expressStatusTimeVo.getFcreateTime());
                    expressBillDetailVo.setContext(ExpressContextEnum.WAREHOUSE_PROCESSING.getDesc());
                    data.add(expressBillDetailVo);
                }
            }
            if(orderStatusTimeVo != null){
                if(orderStatusTimeVo.getFpushTime() != null && !orderStatusTimeVo.getFpushTime().equals("1970-01-01 00:00:00")){
                    ExpressBillDetailVo expressBillDetailVo = new ExpressBillDetailVo();
                    expressBillDetailVo.setFtime(orderStatusTimeVo.getFpushTime());
                    expressBillDetailVo.setContext(ExpressContextEnum.THE_WAREHOUSE_HAS_RECEICED_THE_ORDER.getDesc());
                    data.add(expressBillDetailVo);
                }
                if(orderStatusTimeVo.getFaffirmTime() != null && !orderStatusTimeVo.getFaffirmTime().equals("1970-01-01 00:00:00")){
                    ExpressBillDetailVo expressBillDetailVo = new ExpressBillDetailVo();
                    expressBillDetailVo.setFtime(orderStatusTimeVo.getFaffirmTime());
                    if(orderStatusTimeVo.getForderType().equals(1)){
                        expressBillDetailVo.setContext(ExpressContextEnum.ORDER_PAYMENT_SUCCESSFUL.getDesc());
                    }else{
                        expressBillDetailVo.setContext(ExpressContextEnum.ORDER_PAYMENT_SUCCESSFUL_WAITING_FOR_CUSTOMS_CLEARANCE.getDesc());
                    }
                    data.add(expressBillDetailVo);
                }
                if(orderStatusTimeVo.getFcreateTime() != null && !orderStatusTimeVo.getFcreateTime().equals("1970-01-01 00:00:00")){
                    ExpressBillDetailVo expressBillDetailVo = new ExpressBillDetailVo();
                    expressBillDetailVo.setFtime(orderStatusTimeVo.getFcreateTime());
                    expressBillDetailVo.setContext(ExpressContextEnum.ORDER_SUBMISSION_SUCCESSFUL.getDesc());
                    data.add(expressBillDetailVo);
                }
                expressBillVo.setData(data);
                expressVo.setExpressData(expressBillVo);
            }
        }
        return expressVo;
    }

    @ApiOperation("查询商品订单下所有发货单的物流信息")
    @PostMapping("/queryExpressBatch")
    public Result<List<ExpressVo>> queryExpressBatch(@RequestBody @Validated OrderExpressDto orderExpressDto) {
        Result<List<ExpressVo>> listResult = transportOrderCenterApi.queryExpressBatch(orderExpressDto);
        if(listResult.isSuccess()){
            ExpressDto expressDto = new ExpressDto();
            expressDto.setForderId(orderExpressDto.getForderId());
            if(listResult.getData().size() != 0){
                for(ExpressVo expressVo: listResult.getData()){
                    List<TransportSkuVo> transportSkuVoList = expressVo.getTransportSkuVoList();
                    ExpressStatusTimeVo expressStatusTimeVo = new ExpressStatusTimeVo();
                    if(transportSkuVoList.size() != 0){
                        TransportOrderDto transportOrderDto = new TransportOrderDto();
                        transportOrderDto.setFtransportOrderId(transportSkuVoList.get(0).getFtransportOrderId());
                        Result<ExpressStatusTimeVo> result = transportOrderCenterApi.queryExpressStatusTime(transportOrderDto);
                        if (result.isSuccess()) {
                            if(result.getData() != null){
                                expressStatusTimeVo = result.getData();
                            }
                        }
                    }
                    expressVo = queryOrderStatusTime(expressDto,expressVo,expressStatusTimeVo);
                }
            }else{
                ExpressVo expressVo = new ExpressVo();
                ExpressStatusTimeVo expressStatusTimeVo = new ExpressStatusTimeVo();
                expressVo = queryOrderStatusTime(expressDto,expressVo,expressStatusTimeVo);
                listResult.getData().add(expressVo);
            }
        }
        return listResult;
    }

    @ApiOperation("查询订单状态数量信息")
    @PostMapping("/queryOrderStatusCount")
    public Result<List<OrderStatusVo>> queryOrderStatusCount(HttpServletRequest request) {
        Long fuid = Long.valueOf(request.getHeader("xyid"));
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
	public  Result<AddressVo> confirmReceipt(HttpServletRequest request) {
		Long fuid = Long.valueOf(request.getHeader("xyid"));
        ShipAddressDto  shipAddressDto = new ShipAddressDto();
		shipAddressDto.setFuid(fuid);
		return userDeliveryCenterApi.queryDefaultAddress(shipAddressDto);
	}

    @ApiOperation("查询旧系统是否存在历史订单")
    @PostMapping("/hasOldOrder")
    public Result hasOldOrder(HttpServletRequest request) {
        String token = request.getHeader(ACCESS_TOKEN);
        if(Objects.isNull(token)){
            log.error("token错误!");
            return Result.failure(ResultStatus.PARAM_ERROR);
        }
        return Result.success(getOldOrderStatus(token));
    }

	@ApiOperation("发货提醒")
	@PostMapping("/remindDelivery")
	public Result<Void> remindDelivery(@RequestBody @Validated OnlyOrderIdDto orderIdDto) {
		//异步处理提醒，直接返回前端提醒成功
		orderApi.remindDelivery(orderIdDto);
		return Result.success();
	}

    /**
     * 查询旧订单
     * @return
     */
    private Boolean getOldOrderStatus(String token){
        //返回是否存在历史订单
        Boolean hasOldOrder = false;
        String returnStr = "";
        try {
            URL url = new URL(old_orderList_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            PrintWriter out = null;
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty(ACCESS_TOKEN, token);
            conn.setRequestProperty("isNewAccessToken", "Y");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            out = new PrintWriter(conn.getOutputStream());
            out.flush();
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String str = "";
            com.alibaba.fastjson.JSONObject jsonss = null;
            while ((str = br.readLine()) != null) {
                returnStr = returnStr + str;
            }
            log.info("获取到的报文数据为：" + returnStr);

            Map<String, Object> returnMap = new HashMap<String, Object>();
            if (!Objects.isNull(returnStr)) {
                returnMap = JSON.parseObject(returnStr, HashMap.class);
            }

            //获取分页数据
            if (!Objects.isNull(returnMap) && ResultStatus.OK.getCode().equals(returnMap.get("retCode").toString())) {
                Map<String, Object> retEntity = JSON.parseObject(returnMap.get("retEntity").toString(),HashMap.class);
                //获取总数量
                String totalCount = Objects.isNull(retEntity.get("totalCount"))? "0" : retEntity.get("totalCount").toString();
                if (!Objects.isNull(totalCount) && Integer.parseInt(totalCount) > 0) {
                    hasOldOrder = true;
                }
            }
            is.close();
            conn.disconnect();
        } catch (Exception e) {
            log.error("查询旧系统是否存在历史订单异常!");
            return hasOldOrder;
        }
        return hasOldOrder;
    }
}
