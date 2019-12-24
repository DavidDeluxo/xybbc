package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.ArticleContentDto;
import com.xingyun.bbc.mall.model.vo.ArticleContentVo;
import com.xingyun.bbc.mall.model.vo.ArticleMenuVo;

import java.util.List;

public interface ArticleService {
    Result<List<ArticleMenuVo>> queryArticleMenu();

    Result<ArticleContentVo> queryArticleContent(ArticleContentDto dto);
}
