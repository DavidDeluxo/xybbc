package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户交易明细表
 * @author:lll
 */
@Data
public class UserWalletDetailVo {


    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long fuid;

    /** 明细类型：1,用户支付宝充值 2,用户微信充值 3,用户汇付充值 4,用户线下汇款充值 5,余额下单 6,支付宝下单 7,微信下单 8,余额提现 9,客服取消订单 10,售后退款至余额 11,自动取消订单退款至余额 12,手动取消订单退款至余额 13,售后工单调整余额 14,售后工单调整信用额度 15,账户调整单调整客户账户余额 16,代购收益 */
    @ApiModelProperty("明细类型：1,支付宝充值 2,微信充值 3,网易充值 4,汇款充值 5,订单支付 8,支付宝提现/银行卡提现 10,退款 11,退款 12,退款 13,售后补偿 15,账户调整单调整客户账户余额 16,收益 ")
    private Integer fdetailType;

    /** 明细类型对应不同的单号id */
    @ApiModelProperty("明细类型对应不同的单号id")
    private String ftypeId;

    /** 收入 */
    @ApiModelProperty("收入")
    private BigDecimal fincomeAmount;

    /** 支出 */
    @ApiModelProperty("支出")
    private BigDecimal fexpenseAmount;

    /** 余额 */
    @ApiModelProperty("余额")
    private Long fbalance;


    /** 修改时间 */
    @ApiModelProperty("修改时间")
    private Date fmodifyTime;

    /** 收入合计 */
    @ApiModelProperty("收入合计")
    private BigDecimal fincomeAmountTotal;

    /** 支出合计 */
    @ApiModelProperty("支出合计")
    private BigDecimal fexpenseAmountTotal;

    /** 提现类型:1支付宝，2银行卡 */
    @ApiModelProperty("提现类型:1支付宝，2银行卡")
    private Integer withdrawType;
	
}