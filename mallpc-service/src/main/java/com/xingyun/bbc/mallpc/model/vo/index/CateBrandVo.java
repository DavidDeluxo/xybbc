package com.xingyun.bbc.mallpc.model.vo.index;

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
public class CateBrandVo implements Serializable {

    private static final long serialVersionUID = 929823776893771882L;

    private Long cateId;

    private List<BrandVo> brands;
}
