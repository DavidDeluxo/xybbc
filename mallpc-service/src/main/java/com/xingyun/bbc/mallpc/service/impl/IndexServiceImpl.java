package com.xingyun.bbc.mallpc.service.impl;

import com.xingyun.bbc.mallpc.model.vo.index.BannerVo;
import com.xingyun.bbc.mallpc.model.vo.index.SpecialTopicVo;
import com.xingyun.bbc.mallpc.service.IndexService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexServiceImpl implements IndexService {
    @Override
    public List<SpecialTopicVo> getSpecialTopics() {
        return null;
    }

    @Override
    public List<BannerVo> getBanners() {
        return null;
    }
}
