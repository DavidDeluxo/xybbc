package com.xingyun.bbc.mallpc.model.dto.account;

import com.xingyun.bbc.mallpc.model.validation.extensions.annotations.NumberRange;
import lombok.Data;

@Data
public class AccountDetailDto {

    /**
     * 1 充值 2 提现 3 收支明细
     */
    @NumberRange(values = {1, 2, 3}, message = "明细仅支持充值 提现 收支明细查看")
    private Integer type;

    /**
     * id
     */
    private String id;

}
