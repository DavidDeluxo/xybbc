package com.xingyun.bbc.mallpc.model.dto.withdraw;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author hekaijin
 * @date 2019/9/19 10:46
 * @Description
 */
@ApiModel("提现请求")
@Data
@Accessors(chain = true)
public class WithdrawDto implements Serializable {

    private static final long serialVersionUID = -3375325720449344887L;

    @NotNull(message = "提现金额不能为空1")
    @ApiModelProperty("提现金额")
    private BigDecimal withdrawAmount;

    @NotBlank(message = "提现密码不能为空")
    @ApiModelProperty("提现密码")
    private String withdrawPwd;

    @ApiModelProperty("提现方式|1:支付宝|2:银行卡")
    @Range(min = 1,max = 2,message = "目前不支持此提现方式")
    @NotNull(message = "提现方式不能为空")
    private Integer way;

    @ApiModelProperty(hidden = true)
    private Long uid;

    @ApiModelProperty("支付宝账号")
    private String accountNumber;

    @ApiModelProperty("银行卡号")
    private String cardNumber;

    @ApiModelProperty("银行Code(开户行)")
    private String bankCode;

    @ApiModelProperty("银行名称(开户行)")
    private String bankName;

    @ApiModelProperty("姓名")
    @NotBlank(message = "姓名不能为空")
    private String name;
}
