package com.xingyun.bbc.mall.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchItemListVo<E> extends PageVo {

    private Boolean isLogin;

    public SearchItemListVo(){
        super();
    }

    public SearchItemListVo(Integer totalCount, Integer currentPage, Integer pageSize, List<E> list){
        super(totalCount, currentPage, pageSize, list );
    }

}
