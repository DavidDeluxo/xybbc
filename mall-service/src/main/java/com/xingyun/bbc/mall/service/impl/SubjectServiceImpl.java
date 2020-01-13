package com.xingyun.bbc.mall.service.impl;

import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.SubjectApi;
import com.xingyun.bbc.core.operate.po.Subject;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.model.dto.SubjectQueryDto;
import com.xingyun.bbc.mall.model.vo.SubjectVo;
import com.xingyun.bbc.mall.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/1/13 13:46
 * @description:
 * @package com.xingyun.bbc.mall.service.impl
 */
@Service
public class SubjectServiceImpl implements SubjectService {

    public static final Logger logger = LoggerFactory.getLogger(SubjectServiceImpl.class);

    @Resource
    private SubjectApi subjectApi;

    @Resource
    private DozerHolder dozerHolder;

    @Override
    public SubjectVo getById(SubjectQueryDto subjectQueryDto) {
        Long fsubjectId = subjectQueryDto.getFsubjectId();
        SubjectVo subjectVo = new SubjectVo();
        Result<Subject> subjectResult = subjectApi.queryById(fsubjectId);
        if (!subjectResult.isSuccess()) {
            logger.info("查询专题信息异常，专题id[{}],error:{}", fsubjectId, subjectResult.getMsg());
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        Subject subject = subjectResult.getData();
        if (subject == null) {
            logger.info("专题id[{}]信息不存在", fsubjectId);
            return subjectVo;
        }
        return dozerHolder.convert(subject, SubjectVo.class);
    }
}