package com.xingyun.bbc.mall.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class RefreshCouponDto {

    private Integer fskuIdStart;

    private Integer fskuIdEnd;

    private List<Integer> fskuIds;

    private List<Integer> fcouponIds;

}
