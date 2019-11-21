package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.mallpc.model.dto.account.AccountDetailDto;
import com.xingyun.bbc.mallpc.model.vo.PageVo;
import com.xingyun.bbc.mallpc.model.vo.account.AccountRechargeRecordsVo;
import com.xingyun.bbc.mallpc.model.vo.account.InAndOutRecordsVo;
import com.xingyun.bbc.mallpc.model.vo.account.AccountDetailVo;
import com.xingyun.bbc.mallpc.model.vo.account.WithDrawRecordsVo;

public interface UserAccountService {

    PageVo<AccountRechargeRecordsVo> rechargeRecords(PageVo pageVo,Long uid);

    PageVo<WithDrawRecordsVo> withDrawRecords(PageVo pageVo,Long uid);

    PageVo<InAndOutRecordsVo> inAndOutRecords(PageVo pageVo,Long uid);

    AccountDetailVo accountDetail(AccountDetailDto accountDetailDto);




}
