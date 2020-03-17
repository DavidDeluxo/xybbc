package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.utils.JwtParser;
import com.xingyun.bbc.mallpc.model.dto.subject.SubjectQueryDto;
import com.xingyun.bbc.mallpc.model.vo.TokenInfoVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemListVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemVo;
import com.xingyun.bbc.mallpc.model.vo.subject.ChildSubjectVo;
import com.xingyun.bbc.mallpc.model.vo.subject.SubjectVo;
import com.xingyun.bbc.mallpc.service.SubjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "专题")
@RestController
@RequestMapping("/subject")
public class SubjectController {

    public static final Logger logger = LoggerFactory.getLogger(SubjectController.class);

    @Resource
    private SubjectService subjectService;

    @Autowired
    private JwtParser jwtParser;

    @ApiOperation(value = "查询专题信息", httpMethod = "POST")
    @PostMapping("/via/getById")
    public Result<SubjectVo> getSubjectInfo(@Validated @RequestBody SubjectQueryDto subjectQueryDto) {
        return Result.success(subjectService.getById(subjectQueryDto));
    }

    @ApiOperation(value = "查询专题商品", httpMethod = "POST")
    @PostMapping("/via/searchGoods")
    public Result<SearchItemListVo<SearchItemVo>> getSubjectGoods(@Validated @RequestBody SubjectQueryDto subjectQueryDto, HttpServletRequest request) {
        TokenInfoVo infoVo = jwtParser.getTokenInfo(request);
        subjectQueryDto.setIsLogin(infoVo.getIsLogin());
        subjectQueryDto.setFuid(infoVo.getFuid());
        subjectQueryDto.setFoperateType(infoVo.getFoperateType());
        return Result.success(subjectService.getSubjectGoods(subjectQueryDto));
    }

    @ApiOperation(value = "查询子专题信息", httpMethod = "POST")
    @PostMapping("/via/searchChild")
    public Result<SearchItemListVo<ChildSubjectVo>> getChildSubject(@Validated @RequestBody SubjectQueryDto subjectQueryDto, HttpServletRequest request) {
        TokenInfoVo infoVo = jwtParser.getTokenInfo(request);
        subjectQueryDto.setIsLogin(infoVo.getIsLogin());
        subjectQueryDto.setFuid(infoVo.getFuid());
        subjectQueryDto.setFoperateType(infoVo.getFoperateType());
        return Result.success(subjectService.getChildSubject(subjectQueryDto));
    }


}
