package com.xingyun.bbc.mall.controller;


import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.UserUtil;
import com.xingyun.bbc.mall.model.dto.WithdrawDto;
import com.xingyun.bbc.mall.model.dto.WithdrawRateDto;
import com.xingyun.bbc.mall.model.vo.BanksVo;
import com.xingyun.bbc.mall.model.vo.WalletAmountVo;
import com.xingyun.bbc.mall.model.vo.WithdrawRateVo;
import com.xingyun.bbc.mall.service.WalletService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * @author hekaijin
 * @Description:
 * @createTime: 2019-09-16 14:49
 */
@Api("钱包")
@RestController
@RequestMapping("/wallet")
@Validated
public class WalletController {

    @Autowired
    private WalletService walletService;


    @ApiOperation("获取金额")
    @PostMapping("/amount")
    public Result<WalletAmountVo> amount(HttpServletRequest request) {

        return Result.success(walletService.queryAmount(UserUtil.uid(request)));
    }

    @ApiOperation("提现")
    @PostMapping("/withdraw")
    public Result<Boolean> withdraw(@RequestBody @Valid WithdrawDto withdrawDto, HttpServletRequest request) {

        return Result.success(walletService.withdraw(withdrawDto.setUid(UserUtil.uid(request))));
    }

    @ApiOperation("校验用户是否设置提现支付密码")
    @PostMapping("/hasPayPwd")
    public Result<Boolean> hasPayPwd(HttpServletRequest request) {

        return Result.success(walletService.checkPayPwd(UserUtil.uid(request)));
    }


    @ApiOperation("查询提现手续费费率")
    @PostMapping("/withdrawRate")
    public Result<List<WithdrawRateVo>> withdrawRate(@RequestBody WithdrawRateDto withdrawRateDto) {

        return Result.success(walletService.queryWithdrawRate(withdrawRateDto));
    }

    @ApiOperation("查询银行卡开户行列表")
    @PostMapping("/bankList")
    public Result<List<BanksVo>> bankList(HttpServletRequest request) {

        return Result.success(walletService.queryBankList());
    }

}
