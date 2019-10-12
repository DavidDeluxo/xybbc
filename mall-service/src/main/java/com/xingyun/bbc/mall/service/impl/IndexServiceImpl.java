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
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.base.utils.JacksonUtils;
import com.xingyun.bbc.mall.base.utils.PageUtils;
import com.xingyun.bbc.mall.common.constans.GuidePageContants;
import com.xingyun.bbc.mall.common.constans.PageConfigContants;

import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.dto.CategoryDto;
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
    private GoodsApi goodsApi;

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
                pageTempList = pageConfigVos.stream().filter(index ->{
                    if(index.getFisDelete() != null){
                        boolean a = index.getFisDelete() == 0;
                        boolean b = index.getFposition() == position;
                        boolean c= a && b;
                        return c;
                    }else{
                        boolean b = index.getFposition() == position;
                        return b;
                    }
                   } ).collect(Collectors.toList());
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
     * 查询goodsIds
     *
     * @param fcategoryId1
     * @return List<Long>
     */
    private List<Long> queryGoodIds(Integer fcategoryId1) {
        //查询某一个一级类目下的所有商品
        Criteria<Goods, Object> goodsCriteria = Criteria.of(Goods.class).andEqualTo(Goods::getFisDelete, 0);
        if (null != fcategoryId1) {
            goodsCriteria.andEqualTo(Goods::getFcategoryId1, fcategoryId1);
        }
        Result<List<Goods>> goodsResult = goodsApi.queryByCriteria(goodsCriteria.fields(Goods::getFgoodsId));
        if (!goodsResult.isSuccess()) {
            logger.info("查询商品贸易类型信息失败");
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if (!CollectionUtils.isEmpty(goodsResult.getData())) {
            List<Long> collect = goodsResult.getData().stream().map(goods -> goods.getFgoodsId()).collect(Collectors.toList());
            return collect;
        }
        return null;
    }


    @Override
    public PageVo<IndexSkuGoodsVo> queryGoodsByCategoryId1(CategoryDto categoryDto) {
        //第一步，查询一级类目下所有所有商品包含的sku
        Criteria<GoodsSku, Object> criteria = Criteria.of(GoodsSku.class)
                .andEqualTo(GoodsSku::getFisDelete, 0)
                .andEqualTo(GoodsSku::getFskuStatus, 1);
        List<Long> goodsIds = new ArrayList<>();
        if (categoryDto.getFcategoryId1() != null) {
            //查询一级类目下所有商品
            goodsIds = this.queryGoodIds(Math.toIntExact(categoryDto.getFcategoryId1()));
            if (CollectionUtils.isEmpty(goodsIds)) {
                return new PageVo<>(0, categoryDto.getCurrentPage(), categoryDto.getPageSize(), Lists.newArrayList());
            }
        }
        if (!CollectionUtils.isEmpty(goodsIds)) {
            //关联sku基本信息表
            criteria.andIn(GoodsSku::getFgoodsId, goodsIds);
        }
        // 统计次数
        Result<Integer> totalResult = goodsSkuApi.countByCriteria(criteria);
        if (!totalResult.isSuccess()) {
            logger.info("统计首页商品数量信息失败");
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (0 == totalResult.getData() || Objects.isNull(totalResult.getData())) {
            return new PageVo<>(0, categoryDto.getCurrentPage(), categoryDto.getPageSize(), Lists.newArrayList());
        }
        //查询sku基表信息
        Result<List<GoodsSku>> result = goodsSkuApi.queryByCriteria(
                criteria.fields(
                        GoodsSku::getFskuName, GoodsSku::getFskuCode,
                        GoodsSku::getFgoodsId, GoodsSku::getFskuId,
                        GoodsSku::getFskuThumbImage,
                        GoodsSku::getFisUserTypeDiscount)
                        .page(categoryDto.getCurrentPage(), categoryDto.getPageSize())
        );
        if (!result.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (CollectionUtils.isEmpty(result.getData())) {
            return new PageVo<>(0, categoryDto.getCurrentPage(), categoryDto.getPageSize(), Lists.newArrayList());
        }

        //第二步，封装sku中的缩略图，历史销量，最低价格
        List<IndexSkuGoodsVo> indexSkuGoodsVoList = result.getData().stream().map(goodsSku -> {
            IndexSkuGoodsVo indexSkuGoodsVo = dozerMapper.map(goodsSku, IndexSkuGoodsVo.class);
            //1-------封装历史销量
            Criteria<SkuBatch, Object> skuBatchCriteria = Criteria.of(SkuBatch.class)
                    .andEqualTo(SkuBatch::getFskuId, goodsSku.getFskuId());
            Result<List<SkuBatch>> skuBatchList = skuBatchApi.queryByCriteria(skuBatchCriteria);
            if (!skuBatchList.isSuccess()) {
                throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
            }
            if (CollectionUtils.isEmpty(skuBatchList.getData())) {
                //throw new BizException(MallExceptionCode.SKU_BATCH_IS_NONE);
                return null;
            }
            Criteria<SkuBatch, Object> batchCriteria = Criteria.of(SkuBatch.class)
                    .andEqualTo(SkuBatch::getFskuId, goodsSku.getFskuId())
                    .andEqualTo(SkuBatch::getFbatchStatus, 2);
            Result<List<SkuBatch>> skuBatchs = skuBatchApi.queryByCriteria(batchCriteria);
            if (!skuBatchs.isSuccess()) {
                throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
            }
            if (CollectionUtils.isEmpty(skuBatchs.getData())) {
                //throw new BizException(MallExceptionCode.SKU_BATCH_IS_NONE);
                return null;
            }
            //循环遍历该sku对应的批次集合
            Long fsellNum = 0L;
            for (SkuBatch skuBatch : skuBatchList.getData()) {
                fsellNum += skuBatch.getFsellNum();
            }
            indexSkuGoodsVo.setFsellNum(fsellNum);

      /*      //销量过万，则保留小数量后一位，四舍五入
            if(fsellNum > 10000){
                BigDecimal sellNum = new BigDecimal(fsellNum)
                        .divide(PageConfigContants.BIG_DECIMAL_10000, 1, BigDecimal.ROUND_HALF_UP);
                indexSkuGoodsVo.setFsellNum(String.valueOf(sellNum));
            }else {
                indexSkuGoodsVo.setFsellNum(String.valueOf(fsellNum)+"w");
            }*/

            //封装价格：1.未登录情况下不展示价格：2.根据sku是否支持平台会员类型折扣分两种情况：
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
                //1支持情况下
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
                    //如果没有配置sku会员类型折扣则返回批次价格,有则返回会员类型价格
                    if (CollectionUtils.isEmpty(skuUserDiscountResult.getData())){
                        List<GoodsSkuBatchPrice> salePriceList = new ArrayList<>();
                        for (SkuBatch skuBatch : skuBatchs.getData()) {
                            String supplierSkuBatchId = skuBatch.getFsupplierSkuBatchId();
                            Criteria<GoodsSkuBatchPrice, Object> goodsSkuBatchPriceCriteria = Criteria.of(GoodsSkuBatchPrice.class);
                            goodsSkuBatchPriceCriteria.andEqualTo(GoodsSkuBatchPrice::getFsupplierSkuBatchId, supplierSkuBatchId);
                            Result<List<GoodsSkuBatchPrice>> goodsSkuBatchPriceList = goodsSkuBatchPriceApi.queryByCriteria(goodsSkuBatchPriceCriteria);
                            if (!goodsSkuBatchPriceList.isSuccess()) {
                                throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                            }
                            if (CollectionUtils.isEmpty(goodsSkuBatchPriceList.getData())) {
                                throw new BizException(MallExceptionCode.NO_BATCH_PRICE);
                            }
                            //取不同规格中的最小价格
                            GoodsSkuBatchPrice min = goodsSkuBatchPriceList.getData().stream().min(Comparator.comparing(GoodsSkuBatchPrice::getFbatchSellPrice)).get();
                            salePriceList.add(min);
                        }
                        //取不同批次的最小价格
                        GoodsSkuBatchPrice fbatchSellPrice = salePriceList.stream().min(Comparator.comparing(GoodsSkuBatchPrice::getFbatchSellPrice)).get();
                        //封装关联批次号
                        indexSkuGoodsVo.setFsupplierSkuBatchId(fbatchSellPrice.getFsupplierSkuBatchId());
                        //封装关联包装规格Id
                        indexSkuGoodsVo.setFbatchPackageId(fbatchSellPrice.getFbatchPackageId());
                        //-----------封装价格
                        BigDecimal sellPrice = new BigDecimal(fbatchSellPrice.getFbatchSellPrice())
                                .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
                        indexSkuGoodsVo.setFbatchSellPrice(sellPrice);
                    } else {
                        List<SkuBatchUserPrice> salePriceList = new ArrayList<>();
                        for (SkuBatch skuBatch : skuBatchs.getData()) {
                            String supplierSkuBatchId = skuBatch.getFsupplierSkuBatchId();
                            Criteria<SkuBatchUserPrice, Object> skuBatchUserPriceCriteria = Criteria.of(SkuBatchUserPrice.class);
                            skuBatchUserPriceCriteria
                                    .andEqualTo(SkuBatchUserPrice::getFsupplierSkuBatchId, supplierSkuBatchId)
                                    .andEqualTo(SkuBatchUserPrice::getFuserTypeId, operateType);
                            Result<List<SkuBatchUserPrice>> skuBatchUserPriceList = skuBatchUserPriceApi.queryByCriteria(skuBatchUserPriceCriteria);
                            if (!skuBatchUserPriceList.isSuccess()) {
                                throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                            }
                            if (CollectionUtils.isEmpty(skuBatchUserPriceList.getData())) {
                                logger.info("批次id:::" + "-------------------------------------" + skuBatch.getFskuBatchId());
                                throw new BizException(MallExceptionCode.NO_BATCH_USER_PRICE);
                            }
                            //取不同规格中的最小价格
                            SkuBatchUserPrice min = skuBatchUserPriceList.getData().stream().min(Comparator.comparing(SkuBatchUserPrice::getFbatchSellPrice)).get();
                            salePriceList.add(min);
                        }
                        //取不同批次的最小价格
                        SkuBatchUserPrice fbatchSellPrice = salePriceList.stream().min(Comparator.comparing(SkuBatchUserPrice::getFbatchSellPrice)).get();
                        //封装关联批次号
                        indexSkuGoodsVo.setFsupplierSkuBatchId(fbatchSellPrice.getFsupplierSkuBatchId());
                        //封装关联包装规格Id
                        indexSkuGoodsVo.setFbatchPackageId(fbatchSellPrice.getFbatchPackageId());
                        //--------封装价格
                        BigDecimal sellPrice = new BigDecimal(fbatchSellPrice.getFbatchSellPrice())
                                .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
                        indexSkuGoodsVo.setFbatchSellPrice(sellPrice);
                    }
                } else {
                    //2.不支持情况
                    List<GoodsSkuBatchPrice> salePriceList = new ArrayList<>();
                    for (SkuBatch skuBatch : skuBatchs.getData()) {
                        String supplierSkuBatchId = skuBatch.getFsupplierSkuBatchId();
                        Criteria<GoodsSkuBatchPrice, Object> goodsSkuBatchPriceCriteria = Criteria.of(GoodsSkuBatchPrice.class);
                        goodsSkuBatchPriceCriteria.andEqualTo(GoodsSkuBatchPrice::getFsupplierSkuBatchId, supplierSkuBatchId);
                        Result<List<GoodsSkuBatchPrice>> goodsSkuBatchPriceList = goodsSkuBatchPriceApi.queryByCriteria(goodsSkuBatchPriceCriteria);
                        if (!goodsSkuBatchPriceList.isSuccess()) {
                            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                        }
                        if (CollectionUtils.isEmpty(goodsSkuBatchPriceList.getData())) {
                            throw new BizException(MallExceptionCode.NO_BATCH_PRICE);
                        }
                        //取不同规格中的最小价格
                        GoodsSkuBatchPrice min = goodsSkuBatchPriceList.getData().stream().min(Comparator.comparing(GoodsSkuBatchPrice::getFbatchSellPrice)).get();
                        salePriceList.add(min);
                    }
                    //取不同批次的最小价格
                    GoodsSkuBatchPrice fbatchSellPrice = salePriceList.stream().min(Comparator.comparing(GoodsSkuBatchPrice::getFbatchSellPrice)).get();
                    //封装关联批次号
                    indexSkuGoodsVo.setFsupplierSkuBatchId(fbatchSellPrice.getFsupplierSkuBatchId());
                    //封装关联包装规格Id
                    indexSkuGoodsVo.setFbatchPackageId(fbatchSellPrice.getFbatchPackageId());
                    //-----------封装价格
                    BigDecimal sellPrice = new BigDecimal(fbatchSellPrice.getFbatchSellPrice())
                            .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
                    indexSkuGoodsVo.setFbatchSellPrice(sellPrice);
                }
            }
            return indexSkuGoodsVo;
        }).collect(Collectors.toList());
        List<IndexSkuGoodsVo> list = new ArrayList<>();
        for (IndexSkuGoodsVo indexSkuGoodsVo : indexSkuGoodsVoList) {
            if (indexSkuGoodsVo != null) {
                list.add(indexSkuGoodsVo);
            }
        }
        if (list.size() > 1) {
            Collections.sort(list, new Comparator<IndexSkuGoodsVo>() {

                @Override
                public int compare(IndexSkuGoodsVo o1, IndexSkuGoodsVo o2) {
                    return o2.getFsellNum().compareTo(o1.getFsellNum());
                }
            });
        }
        if (categoryDto.getCurrentPage() > 10) {
            return new PageVo<>(0, categoryDto.getCurrentPage(), categoryDto.getPageSize(), Lists.newArrayList());
        }
        return pageUtils.convert(totalResult.getData(), list, IndexSkuGoodsVo.class, categoryDto);
    }

    @Override
    public Result<List<GoodsCategoryVo>> queryGoodsCategoryList() {
        List<GoodsCategoryVo> categoryVoList = new LinkedList<>();
        Result<List<Goods>> goodsResultAll = goodsApi.queryAll();
        if (!goodsResultAll.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if (!org.springframework.util.CollectionUtils.isEmpty(goodsResultAll.getData())) {
            List<Goods> goodsListAll = goodsResultAll.getData();
            List<Long> categoryListFiltered = goodsListAll.stream().map(Goods::getFcategoryId1).distinct().collect(Collectors.toList());
            Result<List<GoodsCategory>> categoryListResultAll = goodsCategoryApi.queryByCriteria(
                    Criteria.of(GoodsCategory.class)
                            .andIn(GoodsCategory::getFcategoryId, categoryListFiltered)
                            .sortDesc(GoodsCategory::getFmodifyTime)
                            .andEqualTo(GoodsCategory::getFisDelete, 0)
                            .andEqualTo(GoodsCategory::getFisDisplay, 1));
            if (!categoryListResultAll.isSuccess()) {
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            if (!org.springframework.util.CollectionUtils.isEmpty(categoryListResultAll.getData())) {
                for (GoodsCategory category : categoryListResultAll.getData()) {
                    GoodsCategoryVo categoryVo = new GoodsCategoryVo();
                    categoryVo.setFcategoryId(category.getFcategoryId());
                    categoryVo.setFcategoryName(category.getFcategoryName());
                    categoryVo.setFcategoryDesc(category.getFcategoryDesc());
                    categoryVoList.add(categoryVo);
                }

            }
        }
        return Result.success(categoryVoList);
    }


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
