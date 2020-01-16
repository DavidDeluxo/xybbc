package com.xingyun.bbc.mall.service.impl;

import com.xingyun.bbc.common.redis.XyRedisManager;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.ArticleContentApi;
import com.xingyun.bbc.core.operate.api.ArticleMenuApi;
import com.xingyun.bbc.core.operate.api.ArticleMenuRelationApi;
import com.xingyun.bbc.core.operate.po.ArticleContent;
import com.xingyun.bbc.core.operate.po.ArticleMenu;
import com.xingyun.bbc.core.operate.po.ArticleMenuRelation;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.model.dto.ArticleContentDto;
import com.xingyun.bbc.mall.model.vo.ArticleContentVo;
import com.xingyun.bbc.mall.model.vo.ArticleMenuVo;
import com.xingyun.bbc.mall.service.ArticleService;
import io.seata.common.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {
    public static final Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);

    @Autowired
    ArticleMenuApi articleMenuApi;
    @Autowired
    ArticleContentApi articleContentApi;
    @Autowired
    ArticleMenuRelationApi articleMenuRelationApi;
    @Autowired
    private DozerHolder dozerHolder;
    @Autowired
    private XyRedisManager xyRedisManager;

    @Override
    public Result<List<ArticleMenuVo>> queryArticleMenu() {
        List<ArticleMenuVo> articleMenuVoList = new ArrayList<>();
//        List<ArticleMenuVo> results = (List<ArticleMenuVo>)xyRedisManager.get("pc_mall_article_menu");

        Criteria<ArticleMenu, Object> articleMenuCriteria = Criteria.of(ArticleMenu.class)
                .andEqualTo(ArticleMenu::getFmenuType,0)
                .fields(ArticleMenu::getFmenuId,ArticleMenu::getFmenuName,ArticleMenu::getFcolumnId,ArticleMenu::getFmenuSort,
                        ArticleMenu::getFarticleSortType,ArticleMenu::getFarticleShowNumber).sort(ArticleMenu::getFmenuSort);
        Result<List<ArticleMenu>> listResult = articleMenuApi.queryByCriteria(articleMenuCriteria);
        if(!listResult.isSuccess()){
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if(!CollectionUtils.isEmpty(listResult.getData())){
            for(ArticleMenu articleMenu : listResult.getData()){
                ArticleMenuVo articleMenuVo = new ArticleMenuVo();
                articleMenuVo.setFmenuId(articleMenu.getFmenuId());
                articleMenuVo.setFmenuName(articleMenu.getFmenuName());
                Criteria<ArticleContent,Object> articleContentCriteria = Criteria.of(ArticleContent.class);
                if(articleMenu.getFarticleSortType().equals(0)){
                    articleContentCriteria.andEqualTo(ArticleContent::getFcolumnId1,articleMenu.getFcolumnId())
                            .andEqualTo(ArticleContent::getFarticleStatus,1)
                            .fields(ArticleContent::getFarticleId,ArticleContent::getFarticleSubjectId)
                            .page(1,articleMenu.getFarticleShowNumber())
                            .sortDesc(ArticleContent::getFarticleSort);
                }else{
                    Criteria<ArticleMenuRelation,Object> articleMenuRelationCriteria = Criteria.of(ArticleMenuRelation.class)
                            .andEqualTo(ArticleMenuRelation::getFmenuId,articleMenu.getFmenuId())
                            .fields(ArticleMenuRelation::getFarticleId).sort(ArticleMenuRelation::getFarticleCustomizeSort);
                    Result<List<ArticleMenuRelation>> result = articleMenuRelationApi.queryByCriteria(articleMenuRelationCriteria);
                    if(!result.isSuccess()){
                        throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
                    }
                    if(!CollectionUtils.isEmpty(result.getData())){
                        List<Integer> articleList = new ArrayList<>();
                        for(ArticleMenuRelation articleMenuRelation : result.getData()){
                            articleList.add(articleMenuRelation.getFarticleId());
                        }
                        if(articleList.size() != 0){
                            articleContentCriteria.andIn(ArticleContent::getFarticleId,articleList);
                        }
                    }
                }
                Result<List<ArticleContent>> ContentResult = articleContentApi.queryByCriteria(articleContentCriteria);
//                logger.info(articleContentCriteria.buildSql());
                if(!ContentResult.isSuccess()){
                    throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
                }
                if(!CollectionUtils.isEmpty(ContentResult.getData())){
                    List<ArticleContentVo> articleContentVoList = new ArrayList<>();
//                    List<ArticleContentVo> articleContentVoList = dozerHolder.convert(ContentResult.getData(),ArticleContentVo.class);
                    for(ArticleContent articleContent : ContentResult.getData()){
                        ArticleContentVo articleContentVo = new ArticleContentVo();
                        articleContentVo.setFarticleId(articleContent.getFarticleId());
                        articleContentVo.setFarticleSubjectId(articleContent.getFarticleSubjectId());
                        articleContentVo.setFarticleTitle(articleContent.getFarticleSubjectId());
                        articleContentVoList.add(articleContentVo);
                    }
                    articleMenuVo.setArticleContentList(articleContentVoList);
                }
                articleMenuVoList.add(articleMenuVo);
            }
        }
        return Result.success(articleMenuVoList);
    }

    @Override
    public Result<ArticleContentVo> queryArticleContent(ArticleContentDto dto) {
        ArticleContentVo content = new ArticleContentVo();
        if(dto.getFarticleId() != null){
            Criteria<ArticleContent,Object> articleContentCriteria = Criteria.of(ArticleContent.class)
                    .andEqualTo(ArticleContent::getFarticleId, dto.getFarticleId())
                    .andEqualTo(ArticleContent::getFarticleStatus,1)
                    .fields(ArticleContent::getFarticleSubjectId,ArticleContent::getFarticleContent);
            Result<ArticleContent> ContentResult = articleContentApi.queryOneByCriteria(articleContentCriteria);
            if(!ContentResult.isSuccess()){
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            if(ContentResult.getData() != null){
                content.setFarticleSubjectId(ContentResult.getData().getFarticleSubjectId());
                content.setFarticleContent(ContentResult.getData().getFarticleContent());
            }
        }
        return Result.success(content);
    }
}
