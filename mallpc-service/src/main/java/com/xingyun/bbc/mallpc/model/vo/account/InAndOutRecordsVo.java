package com.xingyun.bbc.mallpc.model.vo.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 收支明细
 */
@Data
public class InAndOutRecordsVo extends AccountBaseInfoVo {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fcreateTime;

    //交易完成时间(如果是工单就是传递审核时间 已完成才会显示)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fpassedTime;

    //交易类型
    private Integer ftradeType;
    //充值单号
    private String fdetailId;

    //支出
    private BigDecimal fexpenseAmount;

    //收入
    private BigDecimal fincomeAmount;



}
