package com.xingyun.bbc.mallpc.service;

        import com.xingyun.bbc.mallpc.model.dto.subject.SubjectQueryDto;
        import com.xingyun.bbc.mallpc.model.vo.search.SearchItemListVo;
        import com.xingyun.bbc.mallpc.model.vo.search.SearchItemVo;
        import com.xingyun.bbc.mallpc.model.vo.subject.ChildSubjectVo;
        import com.xingyun.bbc.mallpc.model.vo.subject.SubjectVo;

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
     * 查询子专题
     *
     * @param subjectQueryDto
     * @return
     */
    SearchItemListVo<ChildSubjectVo> getChildSubject(SubjectQueryDto subjectQueryDto);
}
