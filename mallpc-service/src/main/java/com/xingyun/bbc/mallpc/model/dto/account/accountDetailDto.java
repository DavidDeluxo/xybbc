package com.xingyun.bbc.mallpc.model.dto.account;

import lombok.Data;

@Data
public class accountDetailDto {

    /**
     *1 充值 2 提现 3 收支明细
     */
    private Integer type;

    /**
     * 具体的明细类别
     */
    private Integer type1;

    /**
     * id
     */
    private String id;
}
