package com.xingyun.bbc.mall.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuIdVo {

    {
        isAll = false;
    }

    private List<String> fskuIds;

    private Integer pageSize;

    private Integer pageIndex;

    private Integer totalCount;

    private Boolean isAll;

}
