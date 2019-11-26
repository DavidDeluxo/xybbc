package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.recharge.GetQRCodeDTO;
import com.xingyun.bbc.mallpc.model.dto.recharge.OfflineRechargeVoucherDTO;
import com.xingyun.bbc.mallpc.model.dto.recharge.RechargeSubmitDTO;
import com.xingyun.bbc.mallpc.service.RechargeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author pengaoluo
 * @version 1.0.0
 */
@Api(tags = "充值")
@RestController
@RequestMapping("/recharge")
public class RechargeController {

    @Resource
    private RechargeService rechargeService;

    @ApiOperation("提交充值")
    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid RechargeSubmitDTO dto) {
        return Result.success(rechargeService.save(dto));
    }

    @ApiOperation("线下汇款提交凭证")
    @PostMapping("/offlineVoucher")
    public Result offlineVoucher(@RequestBody @Valid OfflineRechargeVoucherDTO dto) {
        rechargeService.offlineVoucher(dto);
        return Result.success();
    }

    @ApiOperation("获取支付二维码")
    @PostMapping("/getQRCode")
    public Result<?> getQRCode(@RequestBody @Valid GetQRCodeDTO dto) {
        return Result.success(rechargeService.getQRCode(dto.getFtransId()));
    }

}
