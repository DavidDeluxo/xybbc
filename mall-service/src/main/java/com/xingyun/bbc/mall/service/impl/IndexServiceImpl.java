package com.xingyun.bbc.mall.service.impl;


import com.google.common.collect.Lists;
import com.xingyun.bbc.common.redis.XyRedisManager;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.GuidePageApi;
import com.xingyun.bbc.core.operate.api.PageConfigApi;
import com.xingyun.bbc.core.operate.po.GuidePage;
import com.xingyun.bbc.core.operate.po.PageConfig;
import com.xingyun.bbc.core.query.Criteria;

import com.xingyun.bbc.core.sku.api.*;
import com.xingyun.bbc.core.sku.po.*;
import com.xingyun.bbc.core.supplier.po.SupplierSku;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.base.utils.JacksonUtils;
import com.xingyun.bbc.mall.base.utils.PageUtils;
import com.xingyun.bbc.mall.common.constans.GuidePageContants;
import com.xingyun.bbc.mall.common.constans.MallConstants;
import com.xingyun.bbc.mall.common.constans.PageConfigContants;

import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.dto.CategoryDto;
import com.xingyun.bbc.mall.model.dto.PageDto;
import com.xingyun.bbc.mall.model.dto.SearchItemDto;
import com.xingyun.bbc.mall.model.vo.*;
import com.xingyun.bbc.mall.service.IndexService;

import org.apache.commons.collections.CollectionUtils;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lll
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
@Service
public class IndexServiceImpl implements IndexService {

    public static final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);

    @Autowired
    private PageConfigApi pageConfigApi;
    @Autowired
    private GuidePageApi guidePageApi;
    @Resource
    private DozerHolder holder;
    @Autowired
    private XyRedisManager xyRedisManager;

    @Autowired
    GoodsCategoryApi goodsCategoryApi;

    @Autowired
    private GoodsSkuApi goodsSkuApi;

    @Autowired
    private Mapper dozerMapper;

    @Autowired
    GoodsThumbImageApi goodsThumbImageApi;

    @Autowired
    SkuBatchApi skuBatchApi;

    @Autowired
    UserApi userApi;

    @Autowired
    GoodsSkuBatchPriceApi goodsSkuBatchPriceApi;

    @Autowired
    SkuBatchUserPriceApi skuBatchUserPriceApi;

    @Autowired
    private SkuUserDiscountConfigApi skuUserDiscountConfigApi;

    @Autowired
    private PageUtils pageUtils;

    @Autowired
    private DozerHolder dozerHolder;

    /**
     * @author lll
     * @version V1.0
     * @Description: 查询首页配置
     * @Param: [fposition]
     * @return: Result<List                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               <                                                                                                                               PageConfigVo>>
     * @date 2019/9/20 13:49
     */
    @Override
    public Result<List<PageConfigVo>> getConfig(Integer position) {
        List<PageConfigVo> pageConfigRedisResultList = this.selectPageConfigRedisList(position);
        //先查询pageConfigRedis key是否有被命中,没有命中则查询数据库
        if (CollectionUtils.isEmpty(pageConfigRedisResultList)) {
            Criteria<PageConfig, Object> pageConfigCriteria = Criteria.of(PageConfig.class);
            pageConfigCriteria.andEqualTo(PageConfig::getFposition, position);
            pageConfigCriteria.andEqualTo(PageConfig::getFisDelete, 0);//查询未删除的
            if (position == 0 || position == 1) {
                pageConfigCriteria.sort(PageConfig::getFsortValue);
            } else {
                pageConfigCriteria.sort(PageConfig::getFlocation);
            }
            Result<List<PageConfig>> pageConfigResult = pageConfigApi.queryByCriteria(pageConfigCriteria);
            if (!pageConfigResult.isSuccess()) {
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            List<PageConfig> pageConfigs = pageConfigResult.getData();
            pageConfigRedisResultList = holder.convert(pageConfigs, PageConfigVo.class);
            //return Result.success(pageConfigRedisResultList);
            if (position == 2) {
                List<PageConfigVo> pageConfigVos = new ArrayList<>();
                for (PageConfigVo pageConfigVo : pageConfigRedisResultList) {
                    Integer viewType = pageConfigVo.getFviewType();
                    Date currentTime = new Date();
                    Date startTime = pageConfigVo.getFperiodStartTime();
                    Date endTime = pageConfigVo.getFpeiodEndTime();
                    if (viewType == 0) {
                        pageConfigVos.add(pageConfigVo);
                    } else {
                        if (startTime.getTime() <= currentTime.getTime() && endTime.getTime() >= currentTime.getTime()) {
                            pageConfigVos.add(pageConfigVo);
                        }
                    }
                }
                return Result.success(pageConfigVos);
            } else {
                return Result.success(pageConfigRedisResultList);
            }
        } else {
            if (position == 2) {
                List<PageConfigVo> pageConfigVos = new ArrayList<>();
                for (PageConfigVo pageConfigVo : pageConfigRedisResultList) {
                    Integer viewType = pageConfigVo.getFviewType();
                    Date currentTime = new Date();
                    Date startTime = pageConfigVo.getFperiodStartTime();
                    Date endTime = pageConfigVo.getFpeiodEndTime();
                    if (viewType == 0) {
                        pageConfigVos.add(pageConfigVo);
                    } else {
                        if (startTime.getTime() <= currentTime.getTime() && endTime.getTime() >= currentTime.getTime()) {
                            pageConfigVos.add(pageConfigVo);
                        }
                    }
                }
                return Result.success(pageConfigVos);
            } else {
                return Result.success(pageConfigRedisResultList);
            }
        }
    }


    private List<PageConfigVo> selectPageConfigRedisList(int position) {
        try {
            logger.info("查询redisKey,position:{}", position);
            List<PageConfigVo> pageTempList = Lists.newArrayList();
            String redisKey = PageConfigContants.PAGE_CONFIG;
            List<Object> result = xyRedisManager.hValues(redisKey);
            if (CollectionUtils.isNotEmpty(result)) {
                List<PageConfig> pageConfigs = holder.convert(result, PageConfig.class);
                List<PageConfigVo> pageConfigVos = holder.convert(pageConfigs, PageConfigVo.class);
                pageTempList = pageConfigVos.stream().filter(index -> {
                    if (index.getFisDelete() != null) {
                        boolean a = index.getFisDelete() == 0;
                        boolean b = index.getFposition() == position;
                        boolean c = a && b;
                        return c;
                    } else {
                        boolean b = index.getFposition() == position;
                        return b;
                    }
                }).collect(Collectors.toList());
                if (position == 0 || position == 1) {
                    Collections.sort(pageTempList, new Comparator<PageConfigVo>() {
                        @Override
                        public int compare(PageConfigVo o1, PageConfigVo o2) {
                            return o1.getFsortValue().compareTo(o2.getFsortValue());
                        }
                    });
                } else {
                    Collections.sort(pageTempList, new Comparator<PageConfigVo>() {
                        @Override
                        public int compare(PageConfigVo o1, PageConfigVo o2) {
                            return o1.getFlocation().compareTo(o2.getFlocation());
                        }
                    });
                }
            }
            return pageTempList;
        } catch (Exception e) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * @author lll
     * @version V1.0
     * @Description: 查询首页楼层商品
     * @Param: [supplierAccountQueryDto]
     * @return: PageVo<SupplierAccountVo>
     * @date 2019/9/20 13:49
     */
    @Override
    public SearchItemListVo<SearchItemVo> queryGoodsByCategoryId1(SearchItemDto searchItemDto) {
        //兼容搜索那边接口做分页处理
        CategoryDto categoryDto =  new CategoryDto();
        categoryDto.setFuid(Long.valueOf(searchItemDto.getFuid()));
        categoryDto.setCurrentPage(searchItemDto.getPageIndex());
        categoryDto.setFcategoryId1(searchItemDto.getFcategoryIdL1().get(0));
        // 初始化PageVo
        SearchItemListVo<SearchItemVo> pageVo = new SearchItemListVo<>();
        pageVo.setIsLogin(searchItemDto.getIsLogin());
        pageVo.setTotalCount(0);
        pageVo.setPageSize(1);
        //第一步，查询一级类目下所有所有未删除且状态为已上架的sku
        if (categoryDto.getFcategoryId1() == null) {
            throw new BizException(MallExceptionCode.NO_USER_CATEGORY_ID);
        }
        Criteria<GoodsSku, Object> criteria = Criteria.of(GoodsSku.class)
                .andEqualTo(GoodsSku::getFisDelete, 0)
                .andEqualTo(GoodsSku::getFcategoryId1, categoryDto.getFcategoryId1())
                .andEqualTo(GoodsSku::getFskuStatus, 1);
        //查询sku基表信息
        Result<List<GoodsSku>> result = goodsSkuApi.queryByCriteria(
                criteria.fields(
                        GoodsSku::getFskuName, GoodsSku::getFskuCode,
                        GoodsSku::getFgoodsId, GoodsSku::getFskuId,
                        GoodsSku::getFskuThumbImage, GoodsSku::getFskuTaxRate,
                        GoodsSku::getFisUserTypeDiscount)
                        .page(categoryDto.getCurrentPage(), categoryDto.getPageSize())
        );
        if (!result.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (CollectionUtils.isEmpty(result.getData())) {
            return new SearchItemListVo<>(0, categoryDto.getCurrentPage(), categoryDto.getPageSize(), Lists.newArrayList());
        }
        // 统计次数
        Result<Integer> totalResult = goodsSkuApi.countByCriteria(criteria);
        if (!totalResult.isSuccess()) {
            logger.info("统计首页商品数量信息失败");
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (0 == totalResult.getData() || Objects.isNull(totalResult.getData())) {
            return new SearchItemListVo<>(0, categoryDto.getCurrentPage(), categoryDto.getPageSize(), Lists.newArrayList());
        }
        //为避免循环查询所以先查出辅表所需阻断
        //取出skuid结果集
        List<Long> skuIdList = new ArrayList<>(result.getData().stream().map(GoodsSku::getFskuId).collect(Collectors.toList()));
        //查询批次表用来封装销量
        Criteria<SkuBatch, Object> skuBatchCriteria = Criteria.of(SkuBatch.class)
                .andIn(SkuBatch::getFskuId, skuIdList);
        Result<List<SkuBatch>> skuBatchList = skuBatchApi.queryByCriteria(skuBatchCriteria.fields(SkuBatch::getFskuId, SkuBatch::getFsellNum));
        if (!skuBatchList.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        //查询批次表此时批次状态为已上架用来封装价格
        Criteria<SkuBatch, Object> batchCriteria = Criteria.of(SkuBatch.class).fields(SkuBatch::getFsupplierSkuBatchId, SkuBatch::getFskuId, SkuBatch::getFbatchPriceType)
                .andIn(SkuBatch::getFskuId, skuIdList)
                .andEqualTo(SkuBatch::getFbatchStatus, 2);
        Result<List<SkuBatch>> skuBatchs = skuBatchApi.queryByCriteria(batchCriteria);
        if (!skuBatchs.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        //第二步，封装sku历史销量，最低价格
        List<SearchItemVo> searchItemVoList = result.getData().stream().map(goodsSku -> {
            SearchItemVo searchItemVo = dozerMapper.map(goodsSku, SearchItemVo.class);
            searchItemVo.setFimgUrl(goodsSku.getFskuThumbImage());
            //1-------封装历史销量
            List<Long> supplierIdList = skuBatchList.getData().stream().filter(s -> s.getFskuId().equals(goodsSku.getFskuId()))
                    .map(SkuBatch::getFsellNum).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(supplierIdList)) {
                //循环遍历累加得到历史销量
                Long fsellNum = 0L;
                for (Long sellNum : supplierIdList) {
                    fsellNum += sellNum;
                }
                searchItemVo.setFsellNum(fsellNum);
            }
            //2---------封装价格：1.未登录情况下不展示价格：2.根据sku是否支持平台会员类型折扣分两种情况：
            // 一，支持，则在t_bbc_sku_batch_user_price取值；二，不支持，则在t_bbc_goods_sku_batch_price取值
            //不同批次不同规格价格不同，取其中最低价展示
            if (categoryDto.getFuid() != null) {
                //登录情况下
                Result<User> user = userApi.queryById(categoryDto.getFuid());
                if (!user.isSuccess()) {
                    throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                }
                if (Objects.isNull(user.getData())) {
                    throw new BizException(MallExceptionCode.NO_USER);
                }
                //先拿到会员类型
                Integer operateType = user.getData().getFoperateType();
                Integer verifyStatus = user.getData().getFverifyStatus();
                //判断sku是否支持平台会员类型折扣 0否 1是
                //1支持会员类型折扣情况下分两种：是否配置了会员类型折扣
                if (goodsSku.getFisUserTypeDiscount().equals(1) && verifyStatus.equals(3)) {
                    //判断sku会员类型折扣是否配置
                    Result<List<SkuUserDiscountConfig>> skuUserDiscountResult = skuUserDiscountConfigApi.queryByCriteria(Criteria.of(SkuUserDiscountConfig.class)
                            .andEqualTo(SkuUserDiscountConfig::getFskuId, goodsSku.getFskuId())
                            .andEqualTo(SkuUserDiscountConfig::getFuserTypeId, operateType)
                            .andEqualTo(SkuUserDiscountConfig::getFisDelete, 0)
                            .fields(SkuUserDiscountConfig::getFdiscountId));
                    if (!skuUserDiscountResult.isSuccess()) {
                        throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                    }
                    //没有配置sku会员类型折扣则返回批次价格
                    if (CollectionUtils.isEmpty(skuUserDiscountResult.getData())) {
                        List<GoodsSkuBatchPrice> salePriceList = new ArrayList<>();
                        List<SkuBatch> skuBatchIdList = skuBatchs.getData().stream().filter(s -> s.getFskuId().equals(goodsSku.getFskuId()))
                                .collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(skuBatchIdList)) {
                            for (SkuBatch skuBatch : skuBatchIdList) {
                                Criteria<GoodsSkuBatchPrice, Object> goodsSkuBatchPriceCriteria = Criteria.of(GoodsSkuBatchPrice.class);
                                goodsSkuBatchPriceCriteria.andEqualTo(GoodsSkuBatchPrice::getFsupplierSkuBatchId, skuBatch.getFsupplierSkuBatchId());
                                Result<List<GoodsSkuBatchPrice>> goodsSkuBatchPriceList = goodsSkuBatchPriceApi.queryByCriteria(goodsSkuBatchPriceCriteria
                                        .fields(GoodsSkuBatchPrice::getFsupplierSkuBatchId, GoodsSkuBatchPrice::getFbatchPackageId, GoodsSkuBatchPrice::getFbatchSellPrice));
                                if (!goodsSkuBatchPriceList.isSuccess()) {
                                    throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                                }
                                if (CollectionUtils.isEmpty(goodsSkuBatchPriceList.getData())) {
                                    throw new BizException(MallExceptionCode.NO_BATCH_PRICE);
                                }
                                //取同一批次中不同规格中的最小价格
                                GoodsSkuBatchPrice min = goodsSkuBatchPriceList.getData().stream().min(Comparator.comparing(GoodsSkuBatchPrice::getFbatchSellPrice)).get();
                                salePriceList.add(min);
                            }
                            //取不同批次的最小价格
                            GoodsSkuBatchPrice fbatchSellPrice = salePriceList.stream().min(Comparator.comparing(GoodsSkuBatchPrice::getFbatchSellPrice)).get();
                            //封装关联批次号
                            //indexSkuGoodsVo.setFsupplierSkuBatchId(fbatchSellPrice.getFsupplierSkuBatchId());
                            //封装关联包装规格Id
                            //indexSkuGoodsVo.setFbatchPackageId(fbatchSellPrice.getFbatchPackageId());
                            //-----------封装价格
                            BigDecimal sellPrice = new BigDecimal(fbatchSellPrice.getFbatchSellPrice())
                                    .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
                            searchItemVo.setFbatchSellPrice(sellPrice);
                        }
                        //有会员折扣配置取会员折扣价
                    } else {
                        List<SkuBatchUserPrice> salePriceList = new ArrayList<>();
                        List<String> supplierSkuBatchIdList = skuBatchs.getData().stream().filter(s -> s.getFskuId().equals(goodsSku.getFskuId()))
                                .map(SkuBatch::getFsupplierSkuBatchId).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(supplierSkuBatchIdList)) {
                            for (String supplierSkuBatchId : supplierSkuBatchIdList) {
                                Criteria<SkuBatchUserPrice, Object> skuBatchUserPriceCriteria = Criteria.of(SkuBatchUserPrice.class)
                                        .andEqualTo(SkuBatchUserPrice::getFsupplierSkuBatchId, supplierSkuBatchId)
                                        .andEqualTo(SkuBatchUserPrice::getFuserTypeId, operateType);
                                Result<List<SkuBatchUserPrice>> skuBatchUserPriceList = skuBatchUserPriceApi.queryByCriteria(skuBatchUserPriceCriteria
                                        .fields(SkuBatchUserPrice::getFsupplierSkuBatchId,
                                                SkuBatchUserPrice::getFbatchPackageId,
                                                SkuBatchUserPrice::getFbatchSellPrice));
                                if (!skuBatchUserPriceList.isSuccess()) {
                                    throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                                }
                                if (CollectionUtils.isEmpty(skuBatchUserPriceList.getData())) {
                                    throw new BizException(MallExceptionCode.NO_BATCH_USER_PRICE);
                                }
                                //取不同规格中的最小价格
                                SkuBatchUserPrice min = skuBatchUserPriceList.getData().stream().min(Comparator.comparing(SkuBatchUserPrice::getFbatchSellPrice)).get();
                                salePriceList.add(min);
                            }
                            //取不同批次的最小价格
                            SkuBatchUserPrice fbatchSellPrice = salePriceList.stream().min(Comparator.comparing(SkuBatchUserPrice::getFbatchSellPrice)).get();
                            //封装关联批次号
                            //indexSkuGoodsVo.setFsupplierSkuBatchId(fbatchSellPrice.getFsupplierSkuBatchId());
                            //封装关联包装规格Id
                            //indexSkuGoodsVo.setFbatchPackageId(fbatchSellPrice.getFbatchPackageId());
                            //--------封装价格
                            BigDecimal sellPrice = new BigDecimal(fbatchSellPrice.getFbatchSellPrice())
                                    .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
                            searchItemVo.setFbatchSellPrice(sellPrice);
                        }
                    }
                } else {
                    //2.不支持会员类型折扣情况下取批次价格
                    List<GoodsSkuBatchPrice> salePriceList = new ArrayList<>();
                    List<SkuBatch> skuBatchIdList = skuBatchs.getData().stream().filter(s -> s.getFskuId().equals(goodsSku.getFskuId()))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(skuBatchIdList)) {
                        for (SkuBatch skuBatch : skuBatchIdList) {
                            Criteria<GoodsSkuBatchPrice, Object> goodsSkuBatchPriceCriteria = Criteria.of(GoodsSkuBatchPrice.class);
                            goodsSkuBatchPriceCriteria.andEqualTo(GoodsSkuBatchPrice::getFsupplierSkuBatchId, skuBatch.getFsupplierSkuBatchId());
                            Result<List<GoodsSkuBatchPrice>> goodsSkuBatchPriceList = goodsSkuBatchPriceApi.queryByCriteria(goodsSkuBatchPriceCriteria
                                    .fields(GoodsSkuBatchPrice::getFsupplierSkuBatchId, GoodsSkuBatchPrice::getFbatchPackageId, GoodsSkuBatchPrice::getFbatchSellPrice));
                            if (!goodsSkuBatchPriceList.isSuccess()) {
                                throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                            }
                            if (CollectionUtils.isEmpty(goodsSkuBatchPriceList.getData())) {
                                throw new BizException(MallExceptionCode.NO_BATCH_PRICE);
                            }
                            //取同一批次中不同规格中的最小价格
                            GoodsSkuBatchPrice min = goodsSkuBatchPriceList.getData().stream().min(Comparator.comparing(GoodsSkuBatchPrice::getFbatchSellPrice)).get();
                            salePriceList.add(min);
                        }
                        //取不同批次的最小价格
                        GoodsSkuBatchPrice fbatchSellPrice = salePriceList.stream().min(Comparator.comparing(GoodsSkuBatchPrice::getFbatchSellPrice)).get();
                        //封装关联批次号
                        //indexSkuGoodsVo.setFsupplierSkuBatchId(fbatchSellPrice.getFsupplierSkuBatchId());
                        //封装关联包装规格Id
                        //indexSkuGoodsVo.setFbatchPackageId(fbatchSellPrice.getFbatchPackageId());
                        //-----------封装价格
                        BigDecimal sellPrice = new BigDecimal(fbatchSellPrice.getFbatchSellPrice())
                                .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
                        searchItemVo.setFbatchSellPrice(sellPrice);
                    }
                }
            }
            return searchItemVo;
        }).collect(Collectors.toList());
        List<SearchItemVo> list = new ArrayList<>();
        for (SearchItemVo searchItemVo : searchItemVoList) {
            if (searchItemVo != null) {
                list.add(searchItemVo);
            }
        }
        if (list.size() > 1) {
            Collections.sort(list, new Comparator<SearchItemVo>() {

                @Override
                public int compare(SearchItemVo o1, SearchItemVo o2) {
                    return o2.getFsellNum().compareTo(o1.getFsellNum());
                }
            });
        }
        if (categoryDto.getCurrentPage() > 10) {
            return new SearchItemListVo<>(0, categoryDto.getCurrentPage(), categoryDto.getPageSize(), Lists.newArrayList());
        }
        pageVo.setPageSize(searchItemDto.getPageSize());
        pageVo.setCurrentPage(searchItemDto.getPageIndex());
        pageVo.setTotalCount(totalResult.getData());
        pageVo.setList(list);
       // return pageUtils.convert(totalResult.getData(), list, SearchItemVo.class,categoryDto );
        return pageVo;
    }

    /**
     * @author lll
     * @version V1.0
     * @Description: 查询商品一级类目列表
     * @Param:
     * @return: Result<List                                                                                                                               <                                                                                                                               GoodsCategoryVo>>
     * @date 2019/9/20 13:49
     */
    @Override
    public Result<List<GoodsCategoryVo>> queryGoodsCategoryList() {
        Result<List<GoodsCategory>> categoryListResultAll = goodsCategoryApi.queryByCriteria(
                Criteria.of(GoodsCategory.class)
                        .fields(GoodsCategory::getFcategoryName, GoodsCategory::getFcategoryId, GoodsCategory::getFcategoryDesc)
                        .sortDesc(GoodsCategory::getFmodifyTime)
                        .andEqualTo(GoodsCategory::getFparentCategoryId, 0)
                        .andEqualTo(GoodsCategory::getFisDelete, 0)
                        .andEqualTo(GoodsCategory::getFisDisplay, 1));
        if (!categoryListResultAll.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        List<GoodsCategoryVo> categoryVoList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(categoryListResultAll.getData())) {
            categoryVoList = dozerHolder.convert(categoryListResultAll.getData(), GoodsCategoryVo.class);
        }
        return Result.success(categoryVoList);
    }

    /**
     * @author fxj
     * @version V1.0
     * @Description: 引导页启动页查询
     * @Param: [ftype]
     * @return: Result<List                                                                                                                               <                                                                                                                               GuidePageVo>>
     * @date 2019/9/20 13:49
     */
    @Override
    public Result<List<GuidePageVo>> selectGuidePageVos(Integer ftype) {
        try {
            Criteria<GuidePage, Object> pageCriteria = Criteria.of(GuidePage.class);
            String redisKey = GuidePageContants.GUIDE_PAGE;
            List<Object> result = xyRedisManager.hValues(redisKey);//先查缓存是否命中,没有命中则查询GuidePageVo
            if (result == null) {
                pageCriteria.andEqualTo(GuidePage::getFtype, ftype);
                Result<List<GuidePage>> res = guidePageApi.queryByCriteria(pageCriteria);
                if (!res.isSuccess()) {
                    throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
                } else {
                    List<GuidePage> guidePageList = res.getData();
                    List<GuidePageVo> convetVoList = JacksonUtils.jsonTolist(JacksonUtils.objectTojson(guidePageList), GuidePageVo.class);
                    return Result.success(convetVoList);
                }
            } else {
                List<GuidePage> guidePage = JacksonUtils.jsonTolist(JacksonUtils.objectTojson(result), GuidePage.class);
                List<GuidePageVo> convetVoList = JacksonUtils.jsonTolist(JacksonUtils.objectTojson(guidePage), GuidePageVo.class);
                List<GuidePageVo> tempList = convetVoList.stream().filter(index -> index.getFtype() == ftype).collect(Collectors.toList());
                return Result.success(tempList);
            }
        } catch (Exception e) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
