package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.utils.RequestHolder;
import com.xingyun.bbc.mallpc.model.dto.account.AccountDetailDto;
import com.xingyun.bbc.mallpc.model.vo.PageVo;
import com.xingyun.bbc.mallpc.model.vo.account.*;
import com.xingyun.bbc.mallpc.service.UserAccountService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("account")
public class UserAccountController {

    @Resource
    private UserAccountService userAccountService;


    @GetMapping("accountInfo")
    public Result<AccountBaseInfoVo> accountInfo() {
        return Result.success(userAccountService.accountInfo(RequestHolder.getUserId()));
    }

    @PostMapping("rechargeRecords")
    public Result<PageVo<AccountRechargeRecordsVo>> rechargeRecords(@RequestBody @Validated PageVo pageVo) {
        return Result.success(userAccountService.rechargeRecords(pageVo, RequestHolder.getUserId()));
    }

    @PostMapping("withDrawRecords")
    public Result<PageVo<WithDrawRecordsVo>> withDrawRecords(@RequestBody @Validated PageVo pageVo) {
        return Result.success(userAccountService.withDrawRecords(pageVo, RequestHolder.getUserId()));
    }

    @PostMapping("inAndOutRecords")
    public Result<PageVo<InAndOutRecordsVo>> inAndOutRecords(@RequestBody @Validated PageVo pageVo) {
        return Result.success(userAccountService.inAndOutRecords(pageVo, RequestHolder.getUserId()));
    }

    @PostMapping("accountDetail")
    public Result<AccountDetailVo> accountDetail(@RequestBody @Validated AccountDetailDto accountDetailDto) {
        return Result.success(userAccountService.accountDetail(accountDetailDto));
    }


}
