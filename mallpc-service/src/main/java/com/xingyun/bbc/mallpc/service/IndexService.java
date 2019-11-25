package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.vo.index.BannerVo;
import com.xingyun.bbc.mallpc.model.vo.index.BrandVo;
import com.xingyun.bbc.mallpc.model.vo.index.GoodsCategoryVo;
import com.xingyun.bbc.mallpc.model.vo.index.SpecialTopicVo;

import java.util.List;
import java.util.Set;

/**
 * @author 陈翔
 * @Title:
 * @Description:
 * @date 2019-11-18 17:00
 */
public interface IndexService {

    /**
     * 专题列表
     * @return
     */
    List<SpecialTopicVo> getSpecialTopics();

    /**
     * Banner列表
     * @return
     */
    List<BannerVo> getBanners();

    /**
     * 首页品牌列表
     * @return
     */
    List<BrandVo> getBrands(Long cateId);

    /**
     * 查询用户数
     * @return
     */
    Integer getUserCount();

    Result<Set<GoodsCategoryVo>> queryCategories();
}
