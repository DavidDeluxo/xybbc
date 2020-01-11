package com.xingyun.bbc.mall.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.order.api.OrderCenterApi;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public Result<AddressVo> confirmReceipt(HttpServletRequest request) {
        Long fuid = Long.valueOf(request.getHeader("xyid"));
        ShipAddressDto shipAddressDto = new ShipAddressDto();
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
