package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.mall.model.vo.VersionVo;

public interface AppVersionService {

    /**
     * 查询最新的app版本更新信息
     * @param fplatform
     * @param version
     * @return
     */
    VersionVo getAppVersionInfo(Integer fplatform, String version);

}
