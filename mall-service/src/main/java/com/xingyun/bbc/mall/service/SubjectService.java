package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.operate.po.Subject;
import com.xingyun.bbc.mall.model.dto.SubjectQueryDto;
import com.xingyun.bbc.mall.model.vo.ChildSubjectVo;
import com.xingyun.bbc.mall.model.vo.SearchItemListVo;
import com.xingyun.bbc.mall.model.vo.SearchItemVo;
import com.xingyun.bbc.mall.model.vo.SubjectVo;

import java.util.List;

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

    /**
     * 根据id获取专题Alias名称
     * @param fsubjectId
     * @return
     */
    String getSubjectAliasName(Long fsubjectId);

    /**
     * 全量更新专题ES商品
     * @param fsubjectIds
     * @param pageSize
     * @param pageIndex
     * @throws Exception
     */
    void updateSubjectInfoToEsByAliasAll(List<Long> fsubjectIds, Integer pageSize, Integer pageIndex);

    /**
     * 查询子专题
     *
     * @param subjectQueryDto
     * @return
     */
    SearchItemListVo<ChildSubjectVo> getChildSubject(SubjectQueryDto subjectQueryDto);
}
