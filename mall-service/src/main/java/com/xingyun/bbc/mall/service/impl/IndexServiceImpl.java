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

import com.xingyun.bbc.mall.common.constans.MallConstants;
import com.xingyun.bbc.mall.common.constans.PageConfigContants;

import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.dto.CategoryDto;

import com.xingyun.bbc.mall.model.dto.SearchItemDto;
import com.xingyun.bbc.mall.model.vo.*;
import com.xingyun.bbc.mall.service.IndexService;

import org.apache.commons.collections.CollectionUtils;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


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
    
    @Autowired
    private SkuBatchPackageApi skuBatchPackageApi;
    
    /**
     * @author lll
     * @version V1.0
     * @Description: 查询首页配置
     * @Param: [fposition]
     * @return: Result<List   <   PageConfigVo>>
     * @date 2019/9/20 13:49
     */
    @Override
    public Result<List<PageConfigVo>> getConfig(Integer position) {
        if (null == position) {
            throw new BizException(MallExceptionCode.REQUIRED_PARAM_MISSING);
        }
        List<PageConfigVo> pageConfigRedisResultList = this.selectPageConfigRedisList(position);
        //先查询pageConfigRedis key是否有被命中,没有命中则查询数据库
        if (CollectionUtils.isEmpty(pageConfigRedisResultList)) {
            Criteria<PageConfig, Object> pageConfigCriteria = Criteria.of(PageConfig.class);
            pageConfigCriteria.andEqualTo(PageConfig::getFposition, position)
            .fields(PageConfig::getFcategoryLevel,PageConfig::getFconfigName
                    ,PageConfig::getFimgUrl,PageConfig::getFlocation,PageConfig::getFpeiodEndTime
                    ,PageConfig::getFperiodStartTime,PageConfig::getFposition
                    ,PageConfig::getFredirectUrl,PageConfig::getFrelationId
                    ,PageConfig::getFsortValue,PageConfig::getFtype
                    ,PageConfig::getFviewType,PageConfig::getFisDelete);
            //查询未删除的
            // pageConfigCriteria.andEqualTo(PageConfig::getFisDelete, 0);
            //查询未删除,配置对象为0的数据
            pageConfigCriteria.andEqualTo(PageConfig::getFisDelete, 0).andEqualTo(PageConfig::getFconfigType, 0);
            //位置为0:Banner配置 1:ICON配置时用sortValue排序字段进行排序
            if (position == 0 || position == 1) {
                pageConfigCriteria.sort(PageConfig::getFsortValue);
            } else {
                //位置为1：专题时用排版对应位置字段location进行排序
                pageConfigCriteria.sort(PageConfig::getFlocation);
            }
            Result<List<PageConfig>> pageConfigResult = pageConfigApi.queryByCriteria(pageConfigCriteria);
            if (!pageConfigResult.isSuccess()) {
                logger.error("查询首页配置失败，首页配置具体位置{}", position);
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            List<PageConfig> pageConfigs = pageConfigResult.getData();
            pageConfigRedisResultList = holder.convert(pageConfigs, PageConfigVo.class);
            //位置为2的数据用展现时间过滤筛选
            return this.getByTime(pageConfigRedisResultList, position);
        } else {
            return this.getByTime(pageConfigRedisResultList, position);
        }
    }
    
    /**
     * @author lll
     * @version V1.0
     * @Description: 首页配置数据通过展现时间筛选
     * @Param: [fposition]
     * @return: Result<ListPageConfigVo>>
     * @date 2019/9/20 13:49
     */
    private Result<List<PageConfigVo>> getByTime(List<PageConfigVo> pageConfigRedisResultList, Integer position) {
        //首页配置位置是2的数据根据展现时间进行过滤
        if (position == 2) {
            List<PageConfigVo> pageConfigVos = new ArrayList<>();
            for (PageConfigVo pageConfigVo : pageConfigRedisResultList) {
                Integer viewType = pageConfigVo.getFviewType();
                Date currentTime = new Date();
                Date startTime = pageConfigVo.getFperiodStartTime();
                Date endTime = pageConfigVo.getFpeiodEndTime();
                //展示类型是长期时不用筛选
                if (viewType == 0) {
                    pageConfigVos.add(pageConfigVo);
                } else {
                    //展示类型是固定周期时不用筛选
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
    
    
    /**
     * @author lll
     * @version V1.0
     * @Description: 先从redis查询首页配置
     * @Param: [fposition]
     * @return: Result<List   <   PageConfigVo>>
     * @date 2019/9/20 13:49
     */
    private List<PageConfigVo> selectPageConfigRedisList(int position) {
        try {
            logger.info("查询redisKey,position:{}", position);
            List<PageConfigVo> pageTempList = Lists.newArrayList();
            //获取key
            String redisKey = PageConfigContants.PAGE_CONFIG;
            List<Object> result = xyRedisManager.hValues(redisKey);
            if (CollectionUtils.isNotEmpty(result)) {
                //对象转换
                List<PageConfig> pageConfigs = holder.convert(result, PageConfig.class);
                List<PageConfigVo> pageConfigVos = holder.convert(pageConfigs, PageConfigVo.class);
                pageTempList = pageConfigVos.stream().filter(index -> {
                    //用位置和是否删除过滤
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
                //位置为0:Banner配置 1:ICON配置时用sortValue排序字段进行排序
                if (position == 0 || position == 1) {
                    Collections.sort(pageTempList, new Comparator<PageConfigVo>() {
                        @Override
                        public int compare(PageConfigVo o1, PageConfigVo o2) {
                            return o1.getFsortValue().compareTo(o2.getFsortValue());
                        }
                    });
                } else {
                    //位置为1：专题时用排版对应位置字段location进行排序
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
     * @Description: 查询用户信息
     * @Param: [userId]
     * @return: Result<User><PageConfigVo>>
     * @date 2019/9/20 13:49
     */
    Result<User> getUser(Integer userId) {
        Result<User> userResult = userApi.queryById(userId);
        if (!userResult.isSuccess()) {
            logger.error("查询用户信息失败 用户id{}", userId);
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (Objects.isNull(userResult.getData())) {
            throw new BizException(MallExceptionCode.NO_USER);
        }
        return Result.success(userResult.getData());
    }
    
    /**
     * @author lll
     * @version V1.0
     * @Description: 查询类目下sku集合
     * @Param: [userId]
     * @return: Result<User><PageConfigVo>>
     * @date 2019/9/20 13:49
     */
    Result<List<GoodsSku>> getSkuList(CategoryDto categoryDto, SearchItemDto searchItemDto) {
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
            logger.error("查询首页楼层商品失败 当前页{}", searchItemDto.getPageIndex());
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        return Result.success(result.getData());
    }
    
    /**
     * @author lll
     * @version V1.0
     * @Description: 查询类目下sku数量
     * @Param: [userId]
     * @return: Result<User><PageConfigVo>>
     * @date 2019/9/20 13:49
     */
    Result<Integer> getSkuCount(CategoryDto categoryDto) {
        Criteria<GoodsSku, Object> criteria = Criteria.of(GoodsSku.class)
                .andEqualTo(GoodsSku::getFisDelete, 0)
                .andEqualTo(GoodsSku::getFcategoryId1, categoryDto.getFcategoryId1())
                .andEqualTo(GoodsSku::getFskuStatus, 1);
        //查询sku基表信息
        Result<Integer> totalResult = goodsSkuApi.countByCriteria(criteria);
        if (!totalResult.isSuccess()) {
            logger.error("统计首页商品数量信息失败");
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        return Result.success(totalResult.getData());
    }
    
    /**
     * @author lll
     * @version V1.0
     * @Description: 查询批次表用来封装销量
     * @Param: [userId]
     * @return: Result<User><PageConfigVo>>
     * @date 2019/9/20 13:49
     */
    Result<List<SkuBatch>> getSkuBatchList(List<Long> skuIdList, SearchItemDto searchItemDto) {
        Criteria<SkuBatch, Object> skuBatchCriteria = Criteria.of(SkuBatch.class)
                .andIn(SkuBatch::getFskuId, skuIdList);
        Result<List<SkuBatch>> skuBatchList = skuBatchApi.queryByCriteria(skuBatchCriteria.fields(SkuBatch::getFskuId, SkuBatch::getFsellNum));
        if (!skuBatchList.isSuccess()) {
            logger.error("查询首页楼层商品批次失败 当前页码{}", searchItemDto.getPageIndex());
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        return Result.success(skuBatchList.getData());
    }
    
    /**
     * @author lll
     * @version V1.0
     * @Description: 查询批次表此时批次状态为已上架用来封装价格
     * @Param: [userId]
     * @return: Result<User><PageConfigVo>>
     * @date 2019/9/20 13:49
     */
    Result<List<SkuBatch>> getSkuBatchListByStatu(List<Long> skuIdList, SearchItemDto searchItemDto) {
        Criteria<SkuBatch, Object> batchCriteria = Criteria.of(SkuBatch.class).fields(
                SkuBatch::getFsupplierSkuBatchId,
                SkuBatch::getFskuId,
                SkuBatch::getFbatchPriceType)
                .andIn(SkuBatch::getFskuId, skuIdList)
                .andEqualTo(SkuBatch::getFbatchStatus, 2);
        Result<List<SkuBatch>> skuBatchs = skuBatchApi.queryByCriteria(batchCriteria);
        if (!skuBatchs.isSuccess()) {
            logger.error("查询首页楼层商品批次失败 当前页码{}", searchItemDto.getPageIndex());
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        return Result.success(skuBatchs.getData());
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
        //校验必传参数类目id
        if (searchItemDto.getFcategoryIdL1() == null) {
            throw new BizException(MallExceptionCode.NO_USER_CATEGORY_ID);
        }
        //兼容搜索那边接口做分页处理
        User user = new User();
        if (null != searchItemDto.getFuid()) {
            if (!searchItemDto.getFverifyStatus().equals(3)) {
                //先查出用户信息避免循环查询
                Result<User> userResult = this.getUser(searchItemDto.getFuid());
                user = userResult.getData();
            }
        }
        CategoryDto categoryDto = new CategoryDto();
        if (null != searchItemDto.getFuid()) {
            categoryDto.setFuid(Long.valueOf(searchItemDto.getFuid()));
        }
        categoryDto.setCurrentPage(searchItemDto.getPageIndex());
        categoryDto.setFcategoryId1(searchItemDto.getFcategoryIdL1().get(0));
        // 初始化PageVo
        SearchItemListVo<SearchItemVo> pageVo = new SearchItemListVo<>();
        pageVo.setIsLogin(searchItemDto.getIsLogin());
        pageVo.setTotalCount(0);
        pageVo.setPageSize(1);
        //第一步，查询一级类目下所有所有未删除且状态为已上架的sku
        Result<List<GoodsSku>> result = this.getSkuList(categoryDto, searchItemDto);
        if (CollectionUtils.isEmpty(result.getData())) {
            return new SearchItemListVo<>(0, categoryDto.getCurrentPage(), categoryDto.getPageSize(), Lists.newArrayList());
        }
        // 统计次数
        Result<Integer> totalResult = this.getSkuCount(categoryDto);
        if (0 == totalResult.getData() || Objects.isNull(totalResult.getData())) {
            return new SearchItemListVo<>(0, categoryDto.getCurrentPage(), categoryDto.getPageSize(), Lists.newArrayList());
        }
        //为避免循环查询所以先查出辅表所需字段
        //取出skuid结果集
        List<Long> skuIdList = new ArrayList<>(result.getData().stream().map(GoodsSku::getFskuId).collect(Collectors.toList()));
        //查询批次表用来封装销量
        Result<List<SkuBatch>> skuBatchList = this.getSkuBatchList(skuIdList, searchItemDto);
        //查询批次表此时批次状态为已上架用来封装价格
        Result<List<SkuBatch>> skuBatchs = this.getSkuBatchListByStatu(skuIdList, searchItemDto);
        //第二步，封装sku历史销量，最低价格
        User finalUser = user;
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
                //先拿到会员类型和认证状态
                Integer operateType;
                Integer verifyStatus;
                //如果认证状态是3：已认证，则取token里面的值
                if (!searchItemDto.getFverifyStatus().equals(3)) {
                    operateType = finalUser.getFoperateType();
                    verifyStatus = finalUser.getFverifyStatus();
                } else {
                    operateType = searchItemDto.getFoperateType();
                    verifyStatus = searchItemDto.getFverifyStatus();
                }
                
                //判断sku是否支持平台会员类型折扣 0否 1是
                //1支持会员类型折扣情况下分两种：是否配置了会员类型折扣，认证是否通过
                if (goodsSku.getFisUserTypeDiscount().equals(1) && verifyStatus.equals(3)) {
                    //判断sku会员类型折扣是否配置
                    Result<List<SkuUserDiscountConfig>> skuUserDiscountResult = skuUserDiscountConfigApi.queryByCriteria
                            (Criteria.of(SkuUserDiscountConfig.class)
                                    .andEqualTo(SkuUserDiscountConfig::getFskuId, goodsSku.getFskuId())
                                    .andEqualTo(SkuUserDiscountConfig::getFuserTypeId, operateType)
                                    .andEqualTo(SkuUserDiscountConfig::getFisDelete, 0)
                                    .fields(SkuUserDiscountConfig::getFdiscountId));
                    if (!skuUserDiscountResult.isSuccess()) {
                        logger.error("查询会员类型折扣配置失败 当前skuId{} 认证类型{}", goodsSku.getFskuId(), operateType);
                        throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                    }
                    //没有配置sku会员类型折扣则返回批次价格
                    if (CollectionUtils.isEmpty(skuUserDiscountResult.getData())) {
                        List<SkuBatch> skuBatchIdList = skuBatchs.getData().stream().filter(s -> s.getFskuId().equals(goodsSku.getFskuId()))
                                .collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(skuBatchIdList)) {
                            //封装最小价格
                            this.getMinPrice(skuBatchIdList, searchItemVo,goodsSku.getFskuTaxRate());
                        }
                        //有会员折扣配置取会员折扣价
                    } else {
                        List<SkuBatch> supplierSkuBatchIdList = skuBatchs.getData().stream().filter(s -> s.getFskuId().equals(goodsSku.getFskuId()))
                                .collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(supplierSkuBatchIdList)) {
                            List<PackagePriceVo> salePriceList = new ArrayList<>();
                            for (SkuBatch skuBatch : supplierSkuBatchIdList) {
                                //获取批次价格类型
                                Integer batchPriceType = skuBatch.getFbatchPriceType();
                                Criteria<SkuBatchUserPrice, Object> skuBatchUserPriceCriteria = Criteria.of(SkuBatchUserPrice.class)
                                        .andEqualTo(SkuBatchUserPrice::getFsupplierSkuBatchId, skuBatch.getFsupplierSkuBatchId())
                                        .andEqualTo(SkuBatchUserPrice::getFuserTypeId, operateType);
                                Result<List<SkuBatchUserPrice>> skuBatchUserPriceList = skuBatchUserPriceApi.queryByCriteria(skuBatchUserPriceCriteria
                                        .fields(SkuBatchUserPrice::getFsupplierSkuBatchId,
                                                SkuBatchUserPrice::getFbatchPackageId,
                                                SkuBatchUserPrice::getFbatchSellPrice));
                                if (!skuBatchUserPriceList.isSuccess()) {
                                    logger.error("查询会员批次价格失败 当前supplierSkuBatchId{}", skuBatch.getFsupplierSkuBatchId());
                                    throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                                }
                                if (CollectionUtils.isEmpty(skuBatchUserPriceList.getData())) {
                                    throw new BizException(MallExceptionCode.NO_BATCH_USER_PRICE);
                                }
                                //通过包装规格id查询包装规格值,用价格除以规格值从而获得单件商品价格
                                List<PackagePriceVo> packagePriceVoList =  new ArrayList<>();
                                for (SkuBatchUserPrice skuBatchUserPrice :skuBatchUserPriceList.getData()) {
                                    //获取包装规格id
                                    Long batchPackageId = skuBatchUserPrice.getFbatchPackageId();
                                    Result<SkuBatchPackage> skuBatchPackage = skuBatchPackageApi.queryById(batchPackageId);
                                    if (!skuBatchPackage.isSuccess()) {
                                        logger.error("查询批次包装规格失败 当前batchPackageId{}", batchPackageId);
                                        throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                                    }
                                    if (StringUtils.isEmpty(skuBatchPackage.getData())) {
                                        throw new BizException(MallExceptionCode.NO_BATCH_PRICE);
                                    }
                                    //获取包装规格值
                                    Long batchPackageNum = skuBatchPackage.getData().getFbatchPackageNum();
                                    //获取单件装价格（单件装价格 = 价格 / 包装规格值）
                                    BigDecimal batchSellPrice = BigDecimal.valueOf(skuBatchUserPrice.getFbatchSellPrice());
                                    BigDecimal onePackagePrice = batchSellPrice.divide(new BigDecimal(batchPackageNum),6,BigDecimal.ROUND_HALF_UP);
                                    //判断批次价格类型是否是2.含邮不含税或者4.不含邮不含税，是的话需要加上税费
                                    if(batchPriceType.equals(2) || batchPriceType.equals(4)){
                                        //税费=价格*税率
                                        BigDecimal tax = batchSellPrice.multiply(new BigDecimal(goodsSku.getFskuTaxRate()))
                                                .divide(MallConstants.TEN_THOUSAND);
                                        onePackagePrice = (batchSellPrice.add(tax)).divide(new BigDecimal(batchPackageNum),6,BigDecimal.ROUND_HALF_UP);
                                    }
                                    //获取单件价格
                                    PackagePriceVo packagePriceVo = new PackagePriceVo();
                                    packagePriceVo.setFbatchSellPrice(onePackagePrice);
                                    packagePriceVoList.add(packagePriceVo);
                                    //skuBatchUserPrice.setFbatchSellPrice(Long.valueOf(String.valueOf(onePackagePrice)));
                                }
                                //取同一批次不同规格中的最小价格
                                PackagePriceVo min = packagePriceVoList.stream().min(Comparator.comparing(PackagePriceVo::getFbatchSellPrice)).get();
                                salePriceList.add(min);
                            }
                            //取不同批次的最小价格
                            PackagePriceVo fbatchSellPrice = salePriceList.stream().min(Comparator.comparing(PackagePriceVo::getFbatchSellPrice)).get();
                            //封装关联批次号
                            //indexSkuGoodsVo.setFsupplierSkuBatchId(fbatchSellPrice.getFsupplierSkuBatchId());
                            //封装关联包装规格Id
                            //indexSkuGoodsVo.setFbatchPackageId(fbatchSellPrice.getFbatchPackageId());
                            //--------封装价格
                            BigDecimal sellPrice = fbatchSellPrice.getFbatchSellPrice()
                                    .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
                            searchItemVo.setFbatchSellPrice(sellPrice);
                        }
                    }
                } else {
                    //2.不支持会员类型折扣情况下取批次价格
                    List<SkuBatch> skuBatchIdList = skuBatchs.getData().stream().filter(s -> s.getFskuId().equals(goodsSku.getFskuId()))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(skuBatchIdList)) {
                        //封装最小价格
                        this.getMinPrice(skuBatchIdList, searchItemVo,goodsSku.getFskuTaxRate());
                    }
                }
            }
            return searchItemVo;
        }).collect(Collectors.toList());
        List<SearchItemVo> list = new ArrayList<>();
        //过滤空数据
        for (SearchItemVo searchItemVo : searchItemVoList) {
            if (searchItemVo != null) {
                list.add(searchItemVo);
            }
        }
        //销量进行排序
        if (list.size() > 1) {
            Collections.sort(list, new Comparator<SearchItemVo>() {
                @Override
                public int compare(SearchItemVo o1, SearchItemVo o2) {
                    return o2.getFsellNum().compareTo(o1.getFsellNum());
                }
            });
        }
        //限制十页200条数据
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
     * @Description: 获取最小价格
     * @Param: searchItemVo
     * @return: void
     * @date 2019/9/20 13:49
     */
    private void getMinPrice(List<SkuBatch> skuBatchIdList, SearchItemVo searchItemVo,Long skuTaxRate) {
        List<PackagePriceVo> salePriceList = new ArrayList<>();
        for (SkuBatch skuBatch : skuBatchIdList) {
            //获取批次价格类型
            Integer batchPriceType = skuBatch.getFbatchPriceType();
            Criteria<GoodsSkuBatchPrice, Object> goodsSkuBatchPriceCriteria = Criteria.of(GoodsSkuBatchPrice.class);
            goodsSkuBatchPriceCriteria.andEqualTo(GoodsSkuBatchPrice::getFsupplierSkuBatchId, skuBatch.getFsupplierSkuBatchId());
            Result<List<GoodsSkuBatchPrice>> goodsSkuBatchPriceList = goodsSkuBatchPriceApi.queryByCriteria(goodsSkuBatchPriceCriteria
                    .fields(GoodsSkuBatchPrice::getFsupplierSkuBatchId, GoodsSkuBatchPrice::getFbatchPackageId, GoodsSkuBatchPrice::getFbatchSellPrice));
            if (!goodsSkuBatchPriceList.isSuccess()) {
                logger.error("查询批次价格失败 当前supplierSkuBatchId{}", skuBatch.getFsupplierSkuBatchId());
                throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
            }
            if (CollectionUtils.isEmpty(goodsSkuBatchPriceList.getData())) {
                throw new BizException(MallExceptionCode.NO_BATCH_PRICE);
            }
            //通过包装规格id查询包装规格值,用价格除以规格值从而获得单件商品价格
            List<PackagePriceVo> packagePriceVoList =  new ArrayList<>();
            for (GoodsSkuBatchPrice goodsSkuBatchPrice :goodsSkuBatchPriceList.getData()) {
                //获取包装规格id
                Long batchPackageId = goodsSkuBatchPrice.getFbatchPackageId();
                Result<SkuBatchPackage> skuBatchPackage = skuBatchPackageApi.queryById(batchPackageId);
                if (!skuBatchPackage.isSuccess()) {
                    logger.error("查询批次包装规格失败 当前batchPackageId{}", batchPackageId);
                    throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                }
                if (StringUtils.isEmpty(skuBatchPackage.getData())) {
                    throw new BizException(MallExceptionCode.NO_BATCH_PRICE);
                }
                //获取包装规格值
                Long batchPackageNum = skuBatchPackage.getData().getFbatchPackageNum();
                //获取单件装价格（单件装价格 = 价格 / 包装规格值）
                BigDecimal batchSellPrice = BigDecimal.valueOf(goodsSkuBatchPrice.getFbatchSellPrice());
                BigDecimal onePackagePrice = batchSellPrice.divide(new BigDecimal(batchPackageNum),6,BigDecimal.ROUND_HALF_UP);
                //判断批次价格类型是否是2.含邮不含税或者4.不含邮不含税，是的话需要加上税费
                if(batchPriceType.equals(2) || batchPriceType.equals(4)){
                    //税费=价格*税率
                    BigDecimal tax = batchSellPrice.multiply(new BigDecimal(skuTaxRate))
                            .divide(MallConstants.TEN_THOUSAND);
                    onePackagePrice = (batchSellPrice.add(tax)).divide(new BigDecimal(batchPackageNum),6,BigDecimal.ROUND_HALF_UP);
                }
                //获取单件价格
                PackagePriceVo packagePriceVo = new PackagePriceVo();
                packagePriceVo.setFbatchSellPrice(onePackagePrice);
                packagePriceVoList.add(packagePriceVo);
                //goodsSkuBatchPrice.setFbatchSellPrice(onePackagePrice.longValue());
            }
            //取同一批次中不同规格中的最小价格
            PackagePriceVo min = packagePriceVoList.stream().min(Comparator.comparing(PackagePriceVo::getFbatchSellPrice)).get();
            salePriceList.add(min);
        }
        //取不同批次的最小价格
        PackagePriceVo fbatchSellPrice = salePriceList.stream().min(Comparator.comparing(PackagePriceVo::getFbatchSellPrice)).get();
        //封装关联批次号
        //indexSkuGoodsVo.setFsupplierSkuBatchId(fbatchSellPrice.getFsupplierSkuBatchId());
        //封装关联包装规格Id
        //indexSkuGoodsVo.setFbatchPackageId(fbatchSellPrice.getFbatchPackageId());
        //-----------封装价格
        BigDecimal sellPrice = fbatchSellPrice.getFbatchSellPrice()
                .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
        searchItemVo.setFbatchSellPrice(sellPrice);
    }
    
    
    /**
     * @author lll
     * @version V1.0
     * @Description: 查询商品一级类目列表
     * @Param:
     * @return: Result<List   <   GoodsCategoryVo>>
     * @date 2019/9/20 13:49
     */
    @Override
    public Result<List<GoodsCategoryVo>> queryGoodsCategoryList() {
        Result<List<GoodsCategory>> categoryListResultAll = goodsCategoryApi.queryByCriteria(
                //查询未删除的，可显示的数据
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
            //类型转换
            categoryVoList = dozerHolder.convert(categoryListResultAll.getData(), GoodsCategoryVo.class);
        }
        return Result.success(categoryVoList);
    }
    
    /**
     * @author fxj
     * @version V1.0
     * @Description: 引导页启动页查询
     * @Param: [ftype]
     * @return: Result<List   <   GuidePageVo>>
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
                Result<List<GuidePage>> res = guidePageApi.queryByCriteria(pageCriteria.andEqualTo(GuidePage::getFguideType, 0));
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
                List<GuidePageVo> tempList = convetVoList.stream().filter(index -> {
                    boolean flag = false;
                    if (index.getFtype() == ftype && index.getFguideType() == 0l) {
                        flag = index.getFtype() == ftype && index.getFguideType() == 0l;
                    }
                    return flag;
                }).collect(Collectors.toList());
                return Result.success(tempList);
            }
        } catch (Exception e) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
