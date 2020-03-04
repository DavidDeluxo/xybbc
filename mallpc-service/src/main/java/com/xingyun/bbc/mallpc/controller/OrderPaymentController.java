package com.xingyun.bbc.mallpc.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xingyun.bbc.core.helper.api.FdfsApi;
import com.xingyun.bbc.core.order.enums.OrderStatus;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.components.RedisHolder;
import com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.ExcelUtils;
import com.xingyun.bbc.mallpc.common.utils.FileUtils;
import com.xingyun.bbc.mallpc.common.utils.RequestHolder;
import com.xingyun.bbc.mallpc.common.utils.ResultUtils;
import com.xingyun.bbc.mallpc.model.dto.pay.OrderExportDto;
import com.xingyun.bbc.mallpc.model.vo.pay.OrderDetailExportVo;
import com.xingyun.bbc.mallpc.model.vo.pay.OrderPaymentExportVo;
import com.xingyun.bbc.mallpc.service.OrderService;
import com.xingyun.bbc.order.api.OrderPaymentCenterApi;
import com.xingyun.bbc.order.model.dto.order.OrderCanelDto;
import com.xingyun.bbc.order.model.dto.order.OrderDetailDto;
import com.xingyun.bbc.order.model.dto.order.QueryOrderListForPCDto;
import com.xingyun.bbc.order.model.vo.order.OrderCancelVo;
import com.xingyun.bbc.order.model.vo.order.OrderDetailVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

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

    public static final Logger logger = LoggerFactory.getLogger(OrderPaymentController.class);

    @Value("${tmpFile.path.saleSku}")
    private String saleSkuFile;

    @Resource
    private OrderPaymentCenterApi orderPaymentApi;

    @Resource
    private OrderService orderService;

    @Resource
    private UserApi userApi;

    @Resource
    private FdfsApi fdfsApi;

    @Resource
    private RedisHolder redisHolder;

    @ApiOperation("查询订单列表")
    @PostMapping("/selectOrderList")
    public Result<JSONObject> selectOrderList(@RequestBody QueryOrderListForPCDto queryOrderListForPCDto) {
        logger.info("查询订单列表：{}", JSON.toJSONString(queryOrderListForPCDto));
        Long fuid = RequestHolder.getUserId();
        queryOrderListForPCDto.setFuid(fuid);
        return orderPaymentApi.selectPCOrderList(queryOrderListForPCDto);
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

    @ApiOperation("导出用户订单列表")
    @GetMapping("/export")
    public void exportOrderList(OrderExportDto orderExportDto, HttpServletResponse response) {
        logger.info("查询导出订单参数：{}", JSON.toJSONString(orderExportDto));
        Long fuid = RequestHolder.getUserId();
        orderExportDto.setFuid(fuid);
        if (orderExportDto.getForderStatus() == null
                || OrderStatus.WAIT_PAYMENT.getCode().equals(orderExportDto.getForderStatus())) {
            ExcelUtils.exportExcelByEasyPoi("支付订单数据", orderExportDto, OrderPaymentExportVo.class, orderService, response);
        } else {
            ExcelUtils.exportExcelByEasyPoi("销售订单数据", orderExportDto, OrderDetailExportVo.class, orderService, response);
        }

    }

    @ApiOperation("导出在售sku")
    @GetMapping("/exportSaleSku")
    public Result<?> exportSaleSku(HttpServletResponse response) {
        User user = ResultUtils.getDataNotNull(userApi.queryById(RequestHolder.getUserId()));
        Integer operateType = user.getFoperateType();
        String fileFdfsPath = redisHolder.get(MallPcRedisConstant.SALE_SKU_TMP_FILE + operateType);
        try {
            byte[] byteArr = ResultUtils.getDataNotNull(fdfsApi.download(fileFdfsPath));
            FileUtils.download(byteArr, response, "saleSku_" + operateType + ".xlsx");
        } catch (Exception e) {
            return Result.failure(MallPcExceptionCode.FILE_NOT_EXIST);
        }
        return Result.success();
    }
}
