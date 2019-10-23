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
    /**
     * @author lll
     * @version V1.0
     * @Description: 查询首页配置
     * @Param: [fposition]
     * @return: Result<List < PageConfigVo>>
     * @date 2019/9/20 13:49
     */
    Result<List<PageConfigVo>> getConfig(Integer fposition);

    /**
     * @author lll
     * @version V1.0
     * @Description: 查询供应商账户
     * @Param: [supplierAccountQueryDto]
     * @return: PageVo<SupplierAccountVo>
     * @date 2019/9/20 13:49
     */
   SearchItemListVo<SearchItemVo> queryGoodsByCategoryId1(SearchItemDto searchItemDto);

    /**
     * @author lll
     * @version V1.0
     * @Description: 查询商品一级类目列表
     * @Param:
     * @return: Result<List   <   GoodsCategoryVo>>
     * @date 2019/9/20 13:49
     */
    Result<List<GoodsCategoryVo>> queryGoodsCategoryList();

    /**
     * @author fxj
     * @version V1.0
     * @Description: 引导页启动页查询
     * @Param: [ftype]
     * @return: Result<List < GuidePageVo>>
     * @date 2019/9/20 13:49
     */
    Result<List<GuidePageVo>> selectGuidePageVos(Integer ftype);
}
