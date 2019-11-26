package com.xingyun.bbc.mallpc.model.vo.search;

import com.xingyun.bbc.mallpc.model.vo.PageVo;
import lombok.Data;

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
