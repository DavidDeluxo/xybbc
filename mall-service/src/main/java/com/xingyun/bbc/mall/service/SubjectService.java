package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.operate.po.Subject;
import com.xingyun.bbc.mall.model.dto.SubjectQueryDto;
import com.xingyun.bbc.mall.model.vo.SearchItemListVo;
import com.xingyun.bbc.mall.model.vo.SearchItemVo;
import com.xingyun.bbc.mall.model.vo.SubjectVo;

public interface SubjectService {

    /**
     * 根据主键查询专题信息
     *
     * @param subjectQueryDto
     * @return
     */
    SubjectVo getById(SubjectQueryDto subjectQueryDto);

    /**
     * 查询专题商品
     *
     * @param subjectQueryDto
     * @return
     */
    SearchItemListVo<SearchItemVo> getSubjectGoods(SubjectQueryDto subjectQueryDto);

    /**
     * 同步ES专题活动sku信息
     * @param subject
     * @throws Exception
     */
    void updateSubjectInfoToEsByAlias(Subject subject) throws Exception;

    /**
     * 删除ES专题活动sku信息
     * @param subject
     */
    void deleteCouponInfoFromEsByAlias(Subject subject);

}
