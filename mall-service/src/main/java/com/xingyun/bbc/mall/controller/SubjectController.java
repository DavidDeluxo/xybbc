package com.xingyun.bbc.mall.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.SubjectQueryDto;
import com.xingyun.bbc.mall.model.vo.SubjectVo;
import com.xingyun.bbc.mall.service.SubjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author lchm
 * @version 1.0.0
 * @date 2020/1/13 13:37
 * @description: 专题
 * @package com.xingyun.bbc.mall.controller
 */
@Api("专题")
@RestController
@RequestMapping("/subject")
public class SubjectController {

    public static final Logger logger = LoggerFactory.getLogger(SubjectController.class);

    @Resource
    private SubjectService subjectService;

    @ApiOperation(value = "查询专题信息", httpMethod = "POST")
    @PostMapping("/via/getById")
    public Result<SubjectVo> getGoodPrice(@Validated @RequestBody SubjectQueryDto subjectQueryDto) {
        return Result.success(subjectService.getById(subjectQueryDto));
    }
}