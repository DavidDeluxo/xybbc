package com.xingyun.bbc.mallpc.model.vo.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 提现详情
 */
@Data
public class AccountDetailVo {
    //提现方式
    private Integer type;
    //姓名
    private String faccountHolder;
    //账号
    private String fwithdrawAccount ;

    //开户行
    private String fwithdrawBank;

    //金额
    private BigDecimal ftransAmount;

    //手续费
    private BigDecimal ftransPoundage;

    //状态
    private Integer ftransStatus;

    //凭证
    private Integer imgurl;

    //实际到账金额 只有提现时才有
    private BigDecimal ftransActualAmount;

    //申请时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fcreateTime;

    //完成时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fpassedTime;


}
