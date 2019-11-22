package com.xingyun.bbc.mallpc.model.vo.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户充值对象
 */
@Data
public class AccountRechargeRecordsVo extends AccountBaseInfoVo {

    @JsonIgnore
    private Date fcreateTime;

    //交易类型
    private Integer frechargeType;

    //充值单号
    private String ftransId;

    //交易金额
    private BigDecimal ftransAmount;

    //交易状态
    private Integer ftransStatus;

    //交易完成时间(如果是工单就是传递审核时间 已完成才会显示)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fpassedTime;


}
