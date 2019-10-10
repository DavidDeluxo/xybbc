package com.xingyun.bbc.mall.service;

import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.model.dto.*;
import com.xingyun.bbc.mall.model.vo.*;
import java.util.List;

/**
 * @author lll
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
public interface IndexService {
    Result<List<PageConfigVo>> getConfig(Integer fposition);

    PageVo<IndexSkuGoodsVo> queryGoodsByCategoryId1(CategoryDto categoryDto);

    Result<List<GoodsCategoryVo>> queryGoodsCategoryList();
    
   /**
    * 
   * @Title: selectGuidePageVos 
   * @Description: 查询引导页启动页
   * @param @param ftype
   * @param @return  参数说明 
   * @return Result<List<GuidePageVo>>    返回类型 
   * @author feixiaojie
   * @throws
    */
    Result<List<GuidePageVo>>selectGuidePageVos(Integer  ftype);
}
