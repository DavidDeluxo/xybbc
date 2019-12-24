package com.xingyun.bbc.mallpc.model.vo.account;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AccountBaseInfoVo implements Serializable {
    //余额
    private BigDecimal banlance;

    //可提现金额
    private BigDecimal cashInAble;

    //提现中金额
    private BigDecimal cashInIng;

    //待收益金额
    private BigDecimal pengingIncome;
}
