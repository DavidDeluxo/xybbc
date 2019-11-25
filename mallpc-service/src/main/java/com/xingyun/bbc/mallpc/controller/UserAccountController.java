package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.utils.RequestHolder;
import com.xingyun.bbc.mallpc.model.dto.account.AccountDetailDto;
import com.xingyun.bbc.mallpc.model.vo.PageVo;
import com.xingyun.bbc.mallpc.model.vo.account.AccountDetailVo;
import com.xingyun.bbc.mallpc.model.vo.account.AccountRechargeRecordsVo;
import com.xingyun.bbc.mallpc.model.vo.account.InAndOutRecordsVo;
import com.xingyun.bbc.mallpc.model.vo.account.WithDrawRecordsVo;
import com.xingyun.bbc.mallpc.service.UserAccountService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("account")
public class UserAccountController {

    @Resource
    private UserAccountService userAccountService;


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
