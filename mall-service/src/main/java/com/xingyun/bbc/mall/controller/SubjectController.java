package com.xingyun.bbc.mall.controller;

import com.xingyun.bbc.core.operate.po.Subject;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.JwtParser;
import com.xingyun.bbc.mall.model.dto.SubjectEsSkuUpdateDto;
import com.xingyun.bbc.mall.model.dto.SubjectQueryDto;
import com.xingyun.bbc.mall.model.vo.SearchItemListVo;
import com.xingyun.bbc.mall.model.vo.SearchItemVo;
import com.xingyun.bbc.mall.model.vo.SubjectVo;
import com.xingyun.bbc.mall.model.vo.TokenInfoVo;
import com.xingyun.bbc.mall.service.SubjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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

    @Resource
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

    @ApiOperation(value = "ES专题商品全量同步", httpMethod = "POST")
    @PostMapping("/via/updateSubjectAliasAll")
    public Result<?> updateAliasAll(@RequestBody SubjectEsSkuUpdateDto subjectEsSkuUpdateDto) {
        subjectService.updateSubjectInfoToEsByAliasAll(
                subjectEsSkuUpdateDto.getFsubjectIds(),
                subjectEsSkuUpdateDto.getPageSize(),
                subjectEsSkuUpdateDto.getPageIndex());
        return Result.success();
    }
}