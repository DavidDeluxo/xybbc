package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.order.api.OrderCouponApi;
import com.xingyun.bbc.order.model.dto.order.OrderSubmitDto;
import com.xingyun.bbc.order.model.vo.coupon.CouponReceiveVo;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.MapUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2019/11/13 9:55
 * @description: TODO
 * @package com.xingyun.bbc.mall.controller
 */
@RestController
@RequestMapping("/coupon")
public class OrderCouponController {

    @Resource
    private OrderCouponApi orderCouponApi;

    @ApiOperation("获取用户当前订单可用优惠券列表")
    @PostMapping("/queryEffective")
    public Result<List<CouponReceiveVo>> queryEffective(@RequestBody @Validated OrderSubmitDto orderSubmitDto, HttpServletRequest request) {
        Long xyid = Long.parseLong(request.getHeader("xyId"));
        orderSubmitDto.setFuid(xyid);
        Result<Map<String, List<CouponReceiveVo>>> result = orderCouponApi.queryEffective(orderSubmitDto);
        Result<List<CouponReceiveVo>> listResult = Result.success(new ArrayList<>());
        if (result.isSuccess()) {
            Map<String, List<CouponReceiveVo>> listMap = result.getData();
            if (MapUtils.isNotEmpty(listMap)) {
                //可用优惠券列表
                listResult = Result.success(listMap.get("usableCouponList"));
            }
        }
        return listResult;
    }

}