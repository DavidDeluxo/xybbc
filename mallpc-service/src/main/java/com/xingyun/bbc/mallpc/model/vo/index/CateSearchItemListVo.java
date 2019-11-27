package com.xingyun.bbc.mallpc.model.vo.index;

import com.xingyun.bbc.mallpc.model.vo.search.SearchItemListVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemVo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author chenxiang
 * @ClassName: BrandVo
 * @Description: 首页分类对应品牌
 * @date 2019年11月18日 14:21:50
 */
@Data
public class CateSearchItemListVo implements Serializable {

    private static final long serialVersionUID = -4945246695307439011L;

    private Integer cateId;

    private SearchItemListVo<SearchItemVo> skus;
}
