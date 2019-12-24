package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.dto.article.ArticleContentDto;
import com.xingyun.bbc.mallpc.model.dto.article.ArticleMenuVo;
import com.xingyun.bbc.mallpc.model.vo.article.ArticleContentVo;

import java.util.List;

public interface ArticleService {
    Result<List<ArticleMenuVo>> queryArticleMenu();

    Result<ArticleContentVo> queryArticleContent(ArticleContentDto dto);
}
