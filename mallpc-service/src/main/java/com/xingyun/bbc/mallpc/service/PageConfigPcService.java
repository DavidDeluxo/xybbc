package com.xingyun.bbc.mallpc.service;

import com.xingyun.bbc.mallpc.model.vo.pageconfig.ModuleVo;
import com.xingyun.bbc.mallpc.model.vo.pageconfig.PageConfigPcVo;

import java.util.List;


public interface PageConfigPcService {

    List<ModuleVo> selectModules();

    List<PageConfigPcVo> navigation();


}
