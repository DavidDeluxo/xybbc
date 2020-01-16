package com.xingyun.bbc.mall.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubjectEsSkuUpdateDto {

    {
        pageSize = 200;
        pageIndex = 1;
    }

    private Integer pageSize;

    private Integer pageIndex;

    private List<Long> fsubjectIds;

}
