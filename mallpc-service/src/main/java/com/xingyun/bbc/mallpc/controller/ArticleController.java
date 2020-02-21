package com.xingyun.bbc.mallpc.controller;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.article.ArticleContentDto;
import com.xingyun.bbc.mallpc.model.dto.article.ArticleMenuVo;
import com.xingyun.bbc.mallpc.model.vo.article.ArticleContentVo;
import com.xingyun.bbc.mallpc.service.ArticleService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/article")
public class ArticleController {
    @Autowired
    ArticleService articleService;

    @ApiOperation("文章菜单栏")
    @PostMapping("/via/queryArticleMenu")
    public Result<List<ArticleMenuVo>> queryArticleMenu(HttpServletRequest request){
        return articleService.queryArticleMenu();
    }

    @ApiOperation("文章内容")
    @PostMapping("/via/queryArticleContent")
    public Result<ArticleContentVo> queryArticleContent(@RequestBody ArticleContentDto dto){
        return articleService.queryArticleContent(dto);
    }

    @ApiOperation("根据协议id集合查询对应文章id")
    @PostMapping(value = "/via/queryArticlesByContractIds")
    public Result<Map<Integer,Integer>> queryArticlesByContractIds(@RequestBody List<Long> contractIds){
        if(CollectionUtils.isEmpty(contractIds)){
            return Result.success(Collections.emptyMap());
        }
        return Result.success(articleService.queryArticlesByContractIds(contractIds));
    }
}
