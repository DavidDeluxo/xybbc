package com.xingyun.bbc.mallpc.model.vo.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 收支明细
 */
@Data
public class InAndOutRecordsVo {


    @JsonIgnore
    private Date fcreateTime;

    //交易完成时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fpassedTime;

    //交易类型
    private Integer fdetailType;

    //单号
    private String ftypeId;

    //支出
    private BigDecimal fexpenseAmount;

    //收入
    private BigDecimal fincomeAmount;


}
