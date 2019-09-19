package com.xingyun.bbc.mall.model.dto;

import javax.persistence.Column;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author:jianghui
 */
@Data
@ApiModel(value = "账号提现")
public class AccountWithdrawDto{


    /** 用户ID */
	@ApiModelProperty(value="用户ID")
    private Long fuid;
	
    /** 开户人名称 */
	@ApiModelProperty(value="开户人名称")
    private String faccountHolder;

    /** 金额 */
    @ApiModelProperty(value="金额")
    private Long ftransAmount;
    
    /** 交易类型：1.充值 2.提现 */ 
    @ApiModelProperty(value="交易类型：1.充值 2.提现 ")
    private Integer ftransTypes;
    
    /** 提现账号 */
	@ApiModelProperty(value="提现账号")
    private String fwithdrawAccount;
	
    /** 提现密码 */
	@ApiModelProperty(value="提现密码")
    private String fwithdrawPasswd;

    /** 提现方式*/
 	@ApiModelProperty(value="提现方式")
    private String fwithdrawType;
 	
    /** 提现银行*/
  	@ApiModelProperty(value="提现方式")
    private String fwithdrawBank;
  	
    /** 原因 */
	@ApiModelProperty(value="原因")
    private String ftransReason;
}