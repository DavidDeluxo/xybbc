package com.xingyun.bbc.mallpc.model.vo.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 提现列表入口
 */
@Data
public class WithDrawRecordsVo extends AccountBaseInfoVo {

    /**
     * 申请时间
     */
    @JsonIgnore
    private Date fcreateTime;

    //提现单号
    private String ftransId;

    //提现金额
    private BigDecimal ftransAmount;

    //手续费
    private BigDecimal ftransPoundage;

    //交易状态
    private Integer ftransStatus;

    //实际到账金额
    private BigDecimal ftransActualAmount;

    //交易完成时间(如果是工单就是传递审核时间 已完成才会显示)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fpassedTime;
}
