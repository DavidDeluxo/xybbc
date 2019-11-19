package com.xingyun.bbc.mall.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.order.api.CouponApi;
import com.xingyun.bbc.order.model.dto.order.OrderSubmitDto;
import com.xingyun.bbc.order.model.vo.coupon.CouponReceiveVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
    private CouponApi couponApi;

    @ApiOperation("获取用户当前订单可用和不可用优惠券列表")
    @PostMapping("/queryEffective")
    public Result<Map<String, List<CouponReceiveVo>>> queryEffective(@RequestBody @Validated OrderSubmitDto orderSubmitDto) {
        return couponApi.queryEffective(orderSubmitDto);
    }

}