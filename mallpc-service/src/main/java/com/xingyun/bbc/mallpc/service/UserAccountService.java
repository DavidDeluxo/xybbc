package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.mallpc.model.dto.PageDto;
import com.xingyun.bbc.mallpc.model.dto.account.AccountDetailDto;
import com.xingyun.bbc.mallpc.model.vo.PageVo;
import com.xingyun.bbc.mallpc.model.vo.account.*;

public interface UserAccountService {

    PageVo<AccountRechargeRecordsVo> rechargeRecords(PageDto pageDto, Long uid);

    PageVo<WithDrawRecordsVo> withDrawRecords(PageDto pageDto, Long uid);

    PageVo<InAndOutRecordsVo> inAndOutRecords(PageDto pageDto, Long uid);

    AccountDetailVo accountDetail(AccountDetailDto accountDetailDto);

    AccountBaseInfoVo accountInfo(Long uid);


}
