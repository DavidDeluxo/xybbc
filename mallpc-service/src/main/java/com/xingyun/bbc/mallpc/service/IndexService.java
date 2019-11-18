package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.model.vo.index.BannerVo;
import com.xingyun.bbc.mallpc.model.vo.index.SpecialTopicVo;

import java.util.List;

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
}
