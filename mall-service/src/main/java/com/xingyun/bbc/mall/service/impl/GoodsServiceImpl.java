package com.xingyun.bbc.mall.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.google.common.collect.Sets;
import com.xingyun.bbc.activity.model.dto.CouponQueryDto;
import com.xingyun.bbc.common.elasticsearch.config.EsBeanUtil;
import com.xingyun.bbc.common.elasticsearch.config.EsCriteria;
import com.xingyun.bbc.common.elasticsearch.config.EsManager;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.market.api.CouponApi;
import com.xingyun.bbc.core.market.api.CouponApplicableSkuApi;
import com.xingyun.bbc.core.market.api.CouponApplicableSkuConditionApi;
import com.xingyun.bbc.core.market.enums.CouponApplicableSkuEnum;
import com.xingyun.bbc.core.market.enums.CouponStatusEnum;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.market.po.CouponApplicableSku;
import com.xingyun.bbc.core.market.po.CouponApplicableSkuCondition;
import com.xingyun.bbc.core.operate.api.PageConfigApi;
import com.xingyun.bbc.core.operate.po.PageConfig;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.*;
import com.xingyun.bbc.core.sku.po.Goods;
import com.xingyun.bbc.core.sku.po.GoodsBrand;
import com.xingyun.bbc.core.sku.po.GoodsCategory;
import com.xingyun.bbc.core.sku.po.GoodsSku;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.enums.UserVerifyStatusEnum;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.enums.MallResultStatus;
import com.xingyun.bbc.mall.base.utils.ResultUtils;
import com.xingyun.bbc.mall.common.ensure.Ensure;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.dto.CouponSkuQueryDto;
import com.xingyun.bbc.mall.model.dto.RefreshCouponDto;
import com.xingyun.bbc.mall.model.dto.SearchItemDto;
import com.xingyun.bbc.mall.model.vo.*;
import com.xingyun.bbc.mall.service.CouponService;
import com.xingyun.bbc.mall.service.GoodsService;
import com.xingyun.bbc.mall.service.SearchRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


@Slf4j
@Service
public class GoodsServiceImpl implements GoodsService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final String PRICE_TYPE_PREFIX_CAMEL = "priceType";

    private static final String PRICE_TYPE_SUFFIX = ".min_price";
    private static Pattern humpPattern = Pattern.compile("[A-Z0-9]");

    private static final String COUPON_ALIAS_PREFIX = "coupon_";

    @Autowired
    EsManager esManager;
    @Autowired
    GoodsCategoryApi goodsCategoryApi;
    @Autowired
    GoodsBrandApi goodsBrandApi;
    @Autowired
    GoodsSkuApi goodsSkuApi;
    @Autowired
    GoodsSearchHistoryApi goodsSearchHistoryApi;
    @Autowired
    PageConfigApi pageConfigApi;
    @Autowired
    SearchRecordService searchRecordService;
    @Autowired
    UserApi userApi;
    @Autowired
    GoodsApi goodsApi;
    @Autowired
    CouponService couponService;

    @Resource
    private CouponApi couponApi;

    @Resource
    private CouponApplicableSkuApi couponApplicableSkuApi;

    @Resource
    private CouponApplicableSkuConditionApi couponApplicableSkuConditionApi;


    @Override
    public Result<SearchFilterVo> searchSkuFilter(SearchItemDto searchItemDto) {

        SearchFilterVo filterVo = new SearchFilterVo();
        this.setCategoryCondition(searchItemDto);
        EsCriteria criteria = EsCriteria.build(searchItemDto);
        if (searchItemDto.getCouponId() != null) {
            criteria.setIndexName(this.getCouponAliasName(Long.parseLong(String.valueOf(searchItemDto.getCouponId()))));
        }
        this.setSearchCondition(searchItemDto, criteria);
        this.setAggregation(criteria);

        Map<String, Object> resultMap = esManager.queryWithAggregation(criteria, true);
        if (resultMap.get("aggregationMap") != null) {
            Map<String, Object> aggregationMap = (Map<String, Object>) resultMap.get("aggregationMap");
            //品牌
            List<Map<String, Object>> barndAggs = (List<Map<String, Object>>) aggregationMap.get("fbrand_id");
            List<BrandFilterVo> brandFilterVoList = EsBeanUtil.getValueObjectList(BrandFilterVo.class, barndAggs);
            filterVo.setBrandList(brandFilterVoList);

            //原产地
            List<Map<String, Object>> originAggs = (List<Map<String, Object>>) aggregationMap.get("forigin_id");
            List<OriginFilterVo> originFilterVoList = EsBeanUtil.getValueObjectList(OriginFilterVo.class, originAggs);

            filterVo.setOriginList(originFilterVoList);

            //贸易类型
            List<Map<String, Object>> tradeAggs = (List<Map<String, Object>>) aggregationMap.get("ftrade_id");
            List<TradeFilterVo> tradeFilterVoList = EsBeanUtil.getValueObjectList(TradeFilterVo.class, tradeAggs);
            filterVo.setTradeList(tradeFilterVoList);

            //商品分类
            List<Map<String, Object>> categoryAggs = (List<Map<String, Object>>) aggregationMap.get("fcategory_id3");
            List<CategoryFilterVo> categoryFilterList = EsBeanUtil.getValueObjectList(CategoryFilterVo.class, categoryAggs);

            //商品属性
            List<Map<String, Object>> attributeAggs = (List<Map<String, Object>>) aggregationMap.get("attribute");
            List<GoodsAttributeFilterVo> attributeFilterList = this.getGoodAttributeList(attributeAggs);
            filterVo.setAttributeFilterList(attributeFilterList);

            Result<List<GoodsCategory>> categoryResult = goodsCategoryApi.queryByCriteria(Criteria.of(GoodsCategory.class));
            if (!categoryResult.isSuccess()) {
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            if (CollectionUtils.isNotEmpty(categoryResult.getData())) {
                Map<Long, List<GoodsCategory>> categoryMap = categoryResult.getData().stream().collect(Collectors.groupingBy(GoodsCategory::getFcategoryId, Collectors.toList()));
                for (CategoryFilterVo categoryFilterVo : categoryFilterList) {
                    Integer fcategoryId = categoryFilterVo.getFcategoryId();
                    List<GoodsCategory> categoryList = categoryMap.get(Long.parseLong(String.valueOf(fcategoryId)));
                    if (CollectionUtils.isNotEmpty(categoryList)) {
                        GoodsCategory category = categoryList.get(0);
                        categoryFilterVo.setFcategorySort(category.getFcategorySort());
                        categoryFilterVo.setFcreateTime(category.getFcreateTime());
                    }
                }
            }
            filterVo.setCategoryList(categoryFilterList);
            Map<String, Object> baseInfoMap = (Map<String, Object>) resultMap.get("baseInfoMap");
            if (MapUtils.isNotEmpty(baseInfoMap) && baseInfoMap.get("totalHits") != null) {
                filterVo.setTotalCount(Integer.parseInt(String.valueOf(baseInfoMap.get("totalHits"))));
            }
        }

        return Result.success(filterVo);
    }

    /**
     * 设定类目条件
     * @param searchItemDto
     */
    private void setCategoryCondition(SearchItemDto searchItemDto) {
        // 商品类目条件
        if (CollectionUtils.isNotEmpty(searchItemDto.getFunicategoryIds())
                && searchItemDto.getFcategoryLevel() != null) {
            Integer fcateogryLevel = searchItemDto.getFcategoryLevel();
            if (fcateogryLevel == 1) {
                searchItemDto.setFcategoryIdL1(searchItemDto.getFunicategoryIds());
            }
            if (fcateogryLevel == 2) {
                searchItemDto.setFcategoryIdL2(searchItemDto.getFunicategoryIds());
            }
            if (fcateogryLevel == 3) {
                searchItemDto.setFcategoryId(searchItemDto.getFunicategoryIds());
            }
        }
    }


    /**
     * 根据贸易类型获取商品
     *
     * @param tradeIds
     * @return
     */
    private List<Goods> getGoodsByTradeIds(List<Long> tradeIds) {
        Criteria<Goods, Object> goodsObjectCriteria = Criteria.of(Goods.class)
                .andIn(Goods::getFtradeId, tradeIds);
        Result<List<Goods>> goodsResult = goodsApi.queryByCriteria(goodsObjectCriteria);
        if (!goodsResult.isSuccess()) {
            throw new BizException(ResultStatus.NOT_IMPLEMENTED);
        }
        return goodsResult.getData();
    }


    /**
     * @author nick
     * @date 2019-11-11
     * @description :  json字符串转java obj
     * @version 1.0.0
     */
    private void parseJsonAndSetFields(CouponSkuQueryDto couponSkuQueryDto, CouponApplicableSkuCondition couponApplicableSkuCondition) {
        try {
            couponSkuQueryDto.setCategoryIds(JSON.parseObject(couponApplicableSkuCondition.getFcategoryId(), Map.class));
            couponSkuQueryDto.setBrandIds(JSON.parseArray(couponApplicableSkuCondition.getFbrandId(), Long.class));
            couponSkuQueryDto.setLabelIds(JSON.parseArray(couponApplicableSkuCondition.getFlabelId(), Long.class));
            couponSkuQueryDto.setTradeIds(JSON.parseArray(couponApplicableSkuCondition.getFtradeCode(), Long.class));
        } catch (JSONException e) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<List<String>> queryHotSearch() {
        List<String> resultList = new LinkedList<>();
        Result<List<PageConfig>> hotSearchResult = pageConfigApi.queryByCriteria(Criteria.of(PageConfig.class)
                .andEqualTo(PageConfig::getFisDelete, 0)
                .andEqualTo(PageConfig::getFtype, 5).sortDesc(PageConfig::getFcreateTime));

        if (!hotSearchResult.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if (CollectionUtils.isEmpty(hotSearchResult.getData())) {
            return Result.success(resultList);
        }

        List<PageConfig> hotSearchList = hotSearchResult.getData();
        resultList = hotSearchList.stream().map(PageConfig::getFconfigName).collect(Collectors.toList());
        return Result.success(resultList);
    }


    @Override
    public Result<BrandPageVo> searchSkuBrandPage(Integer fbrandId) {
        BrandPageVo brandPageVo = new BrandPageVo();
        Result<GoodsBrand> brandResult = goodsBrandApi.queryOneByCriteria(Criteria.of(GoodsBrand.class)
                .andEqualTo(GoodsBrand::getFbrandId, fbrandId));
        if (!brandResult.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if (brandResult.getData() != null) {
            GoodsBrand goodsBrand = brandResult.getData();
            if (goodsBrand.getFisDelete() == 1) {
                throw new BizException(MallResultStatus.BRAND_IS_DELETED);
            }
            brandPageVo.setFbrandLogo(goodsBrand.getFbrandLogo());
            brandPageVo.setFbrandName(goodsBrand.getFbrandName());
            brandPageVo.setFbrandDesc(goodsBrand.getFbrandDesc());
            brandPageVo.setFbrandId(goodsBrand.getFbrandId());
            brandPageVo.setFbrandPoster(goodsBrand.getFbrandPoster());
            brandPageVo.setForiginName(goodsBrand.getFcountryName());

            Result<List<Goods>> goodsResult = goodsApi.queryByCriteria(Criteria.of(Goods.class).andEqualTo(Goods::getFbrandId, fbrandId));
            if (!goodsResult.isSuccess()) {
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            List<Long> goodIds = new LinkedList<>();
            if (CollectionUtils.isNotEmpty(goodsResult.getData())) {
                goodIds = goodsResult.getData().stream().map(Goods::getFgoodsId).collect(Collectors.toList());
            }
            // 品牌sku商品总数
            brandPageVo.setFgoodsTotalCount(0);
            if (CollectionUtils.isNotEmpty(goodIds)) {
                Result<Integer> goodsCountResult = goodsSkuApi.countByCriteria(Criteria.of(GoodsSku.class)
                        .andIn(GoodsSku::getFgoodsId, goodIds)
                        .andEqualTo(GoodsSku::getFskuStatus, 1)
                        .andEqualTo(GoodsSku::getFisDelete, 0));
                if (!goodsCountResult.isSuccess()) {
                    throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
                }
                brandPageVo.setFgoodsTotalCount(goodsCountResult.getData());
            }
        }
        return Result.success(brandPageVo);
    }


    @Override
    public void updateEsSkuWithBaseInfo(Map<String, Object> skuSourceMap, boolean isBaseInfoUpdate){
        if (MapUtils.isEmpty(skuSourceMap)) {
            return;
        }
        if (Objects.isNull(skuSourceMap.get("fsku_id"))) {
            throw new IllegalArgumentException("fsku_id为空");
        }
        Map<String, Object> updateSourceMap = null;
        String fskuId = String.valueOf(skuSourceMap.get("fsku_id"));
        try {
            GetResponse getResponse = esManager.getSourceById(fskuId);
            if (getResponse.isExists()) {
                log.info("更新sku:{}", fskuId);
                Map<String, Object> oldSourceMap = getResponse.getSourceAsMap();
                oldSourceMap.putAll(skuSourceMap);
                updateSourceMap = oldSourceMap;
            } else {
                if (isBaseInfoUpdate) {
                    updateSourceMap = skuSourceMap;
                } else {
                    return;
                }
            }
            Map<String, Map<String, Object>> indexMap = new HashMap<>();
            indexMap.put(fskuId, updateSourceMap);
            esManager.indexInBulk(indexMap);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    @Override
    public void deleteCouponInfoFromEsByAlias(Coupon coupon) {
        try {
            Long fcouponId = coupon.getFcouponId();
            String aliasName = this.getCouponAliasName(fcouponId);
            if (esManager.isAliasExist(aliasName)) {
                AliasActions deleteAction = new AliasActions(AliasActions.Type.REMOVE).alias(aliasName);
                esManager.updateAlias(deleteAction);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void updateCouponInfoToEsByAliasBatch(RefreshCouponDto refreshCouponDto) {
        Integer pageSize = 10;
        Integer pageCount = 0;
        List<Integer> couponStatusList = new LinkedList<>();
        couponStatusList.add(CouponStatusEnum.PUSHED.getCode());
        couponStatusList.add(CouponStatusEnum.OVERDUE.getCode());
        couponStatusList.add(CouponStatusEnum.FINISHED.getCode());
        Criteria<Coupon, Object> couponCriteria = Criteria.of(Coupon.class).fields(Coupon::getFcouponId).andIn(Coupon::getFcouponStatus, couponStatusList);
        if (CollectionUtils.isNotEmpty(refreshCouponDto.getFcouponIds())) {
            couponCriteria.andIn(Coupon::getFcouponId, refreshCouponDto.getFcouponIds());
        }
        Result<Integer> couponCountResult = couponApi.countByCriteria(couponCriteria);
        Ensure.that(couponCountResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
        //分页
        Integer totalCount = couponCountResult.getData();
        if (totalCount % pageSize == 0) {
            pageCount = totalCount / pageSize;
        } else {
            pageCount = totalCount / pageSize + 1;
        }
        log.info("优惠券全量更新开始,入参:{}, 优惠券总数:{}, 分页大小:{}, 总页数：{}", JSON.toJSONString(refreshCouponDto), totalCount, pageSize, pageCount);
        for (int pageIndex = 1; pageIndex <= pageCount; pageIndex++) {
            log.info("优惠券全量更新,第{}页", pageIndex);
            Result<List<Coupon>> pagedCouponResult = couponApi.queryByCriteria(couponCriteria.page(pageIndex, pageSize));
            Ensure.that(pagedCouponResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
            List<Coupon> couponList = pagedCouponResult.getData();
            for (Coupon coupon : couponList) {
                log.info("优惠券全量更新,第{}页, 优惠券,fcounponId:{}", pageIndex, coupon.getFcouponId());
                try {
                    this.updateCouponInfoToEsByAlias(coupon, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void updateCouponInfoToEsByAlias(Coupon coupon, boolean isUpdateByMessage) throws Exception {
        //查询优惠券信息
        Result<Coupon> couponResult = couponApi.queryOneByCriteria(Criteria.of(Coupon.class).andEqualTo(Coupon::getFcouponId, coupon.getFcouponId()));
        Ensure.that(couponResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
        if (Objects.isNull(couponResult.getData())) {
            log.error("查询不到优惠券信息, id:{}", coupon.getFcouponId());
            return;
        }
        //校验优惠券状态
        Coupon couponDataBase = couponResult.getData();
        if (isUpdateByMessage && !couponDataBase.getFcouponStatus().equals(CouponStatusEnum.PUSHED.getCode())) {
            log.error("优惠券不是已发布状态, id:{}, status:{}", coupon.getFcouponId(), couponDataBase.getFcouponStatus());
            return;
        }
        //校验优惠券Alias是否存在
        String aliasName = getCouponAliasName(coupon.getFcouponId());
        AliasActions action = new AliasActions(AliasActions.Type.ADD).alias(aliasName);
        if (esManager.isAliasExist(aliasName)) {
            AliasActions deleteAction = new AliasActions(AliasActions.Type.REMOVE).alias(aliasName);
            esManager.updateAlias(deleteAction);
        }
        this.setActionFilter(couponDataBase, action);
        esManager.updateAlias(action);
    }

    /**
     * @param coupon
     * @param action
     */
    private void setActionFilter(Coupon coupon, AliasActions action) {
        //全部商品可用
        if (coupon.getFapplicableSku().equals(CouponApplicableSkuEnum.ALL.getCode())) {
            return;
        }
        //指定商品可用
        if (coupon.getFapplicableSku().equals(CouponApplicableSkuEnum.SOME.getCode())) {
            //指定sku
            List<String> applicableSkuIds = this.getApplicableSkuIds(coupon);
            if (CollectionUtils.isNotEmpty(applicableSkuIds)) {
                IdsQueryBuilder queryBuilder = new IdsQueryBuilder();
                queryBuilder.addIds(EsCriteria.listToArray(applicableSkuIds));
                action.filter(queryBuilder);
                return;
            }
            //指定sku条件
            List<CouponSkuQueryDto> conditionList = this.getApplicableSkuCondition(coupon);
            Ensure.that(!CollectionUtils.isEmpty(conditionList)).isTrue(MallExceptionCode.SYSTEM_ERROR);
            DisMaxQueryBuilder orConditions = new DisMaxQueryBuilder();
            for (CouponSkuQueryDto skuQueryDto : conditionList) {
                BoolQueryBuilder condition = new BoolQueryBuilder();

                List<Long> oneLevel = Lists.newArrayList();
                List<Long> twoLevel = Lists.newArrayList();
                List<Long> threeLevel = Lists.newArrayList();
                if (skuQueryDto.getCategoryIds() != null) {
                    oneLevel = skuQueryDto.getCategoryIds().get("1");
                    twoLevel = skuQueryDto.getCategoryIds().get("2");
                    threeLevel = skuQueryDto.getCategoryIds().get("3");
                }
                // 品牌id
                List<Long> brandIds = skuQueryDto.getBrandIds();
                // 标签id
                List<Long> labelIds = skuQueryDto.getLabelIds();
                // 贸易类型
                List<Long> tradeIds = skuQueryDto.getTradeIds();
                // sku code
                String skuCode = skuQueryDto.getSkuCode();
                // 商品id
                List<Long> goodsIds = null;
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tradeIds)) {
                    List<Goods> goods = getGoodsByTradeIds(tradeIds);
                    goodsIds = goods.stream().map(Goods::getFgoodsId).collect(Collectors.toList());
                }
                // 分类间或关系开始
                if (CollectionUtils.isNotEmpty(oneLevel) || CollectionUtils.isNotEmpty(twoLevel) || CollectionUtils.isNotEmpty(threeLevel)) {
                    DisMaxQueryBuilder orCategory = new DisMaxQueryBuilder();
                    if (CollectionUtils.isNotEmpty(oneLevel)) {
                        orCategory.add(QueryBuilders.termsQuery("fcategory_id1", oneLevel));
                    }
                    if (CollectionUtils.isNotEmpty(twoLevel)) {
                        orCategory.add(QueryBuilders.termsQuery("fcategory_id2", twoLevel));
                    }
                    if (CollectionUtils.isNotEmpty(threeLevel)) {
                        orCategory.add(QueryBuilders.termsQuery("fcategory_id3", threeLevel));
                    }
                    condition.must(orCategory);
                }
                //品牌
                if (CollectionUtils.isNotEmpty(brandIds)) {
                    condition.must(QueryBuilders.termsQuery("fbrand_id", brandIds));
                }
                //标签
                if (CollectionUtils.isNotEmpty(labelIds)) {
                    condition.must(QueryBuilders.termsQuery("flabel_id", labelIds));
                }
                //商品id
                if (CollectionUtils.isNotEmpty(goodsIds)) {
                    condition.must(QueryBuilders.termsQuery("fgoods_id", goodsIds));
                }
                orConditions.add(condition);
            }
            action.filter(orConditions);
        }

        //指定商品不可用
        if (coupon.getFapplicableSku().equals(CouponApplicableSkuEnum.SOME_NOT.getCode())) {
            //指定sku
            List<String> applicableSkuIds = this.getApplicableSkuIds(coupon);
            if (CollectionUtils.isNotEmpty(applicableSkuIds)) {
                IdsQueryBuilder queryBuilder = new IdsQueryBuilder();
                queryBuilder.addIds(EsCriteria.listToArray(applicableSkuIds));
                QueryBuilder notIds = QueryBuilders.boolQuery().mustNot(queryBuilder);
                action.filter(notIds);
                return;
            }
            //指定sku条件
            List<CouponSkuQueryDto> conditionList = this.getApplicableSkuCondition(coupon);
            Ensure.that(!CollectionUtils.isEmpty(conditionList)).isTrue(MallExceptionCode.SYSTEM_ERROR);
//            DisMaxQueryBuilder orConditions = new DisMaxQueryBuilder();
            BoolQueryBuilder andCondition = new BoolQueryBuilder();
            for (CouponSkuQueryDto skuQueryDto : conditionList) {
                BoolQueryBuilder condition = new BoolQueryBuilder();

                List<Long> oneLevel = skuQueryDto.getCategoryIds().get("1");
                List<Long> twoLevel = skuQueryDto.getCategoryIds().get("2");
                List<Long> threeLevel = skuQueryDto.getCategoryIds().get("3");
                // 品牌id
                List<Long> brandIds = skuQueryDto.getBrandIds();
                // 标签id
                List<Long> labelIds = skuQueryDto.getLabelIds();
                // 贸易类型
                List<Long> tradeIds = skuQueryDto.getTradeIds();
                // sku code
                String skuCode = skuQueryDto.getSkuCode();
                // 商品id
                List<Long> goodsIds = null;
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tradeIds)) {
                    List<Goods> goods = getGoodsByTradeIds(tradeIds);
                    goodsIds = goods.stream().map(Goods::getFgoodsId).collect(Collectors.toList());
                }
                // 分类间或关系开始
                if (CollectionUtils.isNotEmpty(oneLevel) || CollectionUtils.isNotEmpty(twoLevel) || CollectionUtils.isNotEmpty(threeLevel)) {
//                    DisMaxQueryBuilder orCategory = new DisMaxQueryBuilder();
                    BoolQueryBuilder andCategory = QueryBuilders.boolQuery();
                    if (CollectionUtils.isNotEmpty(oneLevel)) {
                        andCategory.must(QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery("fcategory_id1", oneLevel)));
                    }
                    if (CollectionUtils.isNotEmpty(twoLevel)) {
                        andCategory.must(QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery("fcategory_id2", twoLevel)));
                    }
                    if (CollectionUtils.isNotEmpty(threeLevel)) {
                        andCategory.must(QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery("fcategory_id3", threeLevel)));
                    }
                    condition.must(andCategory);
                }
                //品牌
                if (CollectionUtils.isNotEmpty(brandIds)) {
                    condition.must(QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery("fbrand_id", brandIds)));
                }
                //标签
                if (CollectionUtils.isNotEmpty(labelIds)) {
                    condition.must(QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery("flabel_id", labelIds)));
                }
                //商品id
                if (CollectionUtils.isNotEmpty(goodsIds)) {
                    condition.must(QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery("fgoods_id", goodsIds)));
                }
                andCondition.must(condition);
            }
            action.filter(andCondition);
        }
    }

    private List<CouponSkuQueryDto> getApplicableSkuCondition(Coupon coupon) {
        List<CouponSkuQueryDto> couponSkuQueryDtos = Lists.newArrayList();
        Result<List<CouponApplicableSkuCondition>> conditionResult = couponApplicableSkuConditionApi.queryByCriteria(Criteria.of(CouponApplicableSkuCondition.class)
                .andEqualTo(CouponApplicableSkuCondition::getFcouponId, coupon.getFcouponId()));
        Ensure.that(conditionResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
        List<CouponApplicableSkuCondition> conditionList = conditionResult.getData();
        if (CollectionUtils.isEmpty(conditionList)) {
            return couponSkuQueryDtos;
        }
        conditionList.stream().forEach(couponApplicableSkuCondition -> {
            //对应字段转成java object
            CouponSkuQueryDto couponSkuQueryDto = new CouponSkuQueryDto();
            parseJsonAndSetFields(couponSkuQueryDto, couponApplicableSkuCondition);
            couponSkuQueryDtos.add(couponSkuQueryDto);
        });
        return couponSkuQueryDtos;
    }

    /**
     * 根据优惠券id查询指定可用skuId
     * @param coupon
     * @return
     */
    private List<String> getApplicableSkuIds(Coupon coupon) {
        Criteria<CouponApplicableSku, Object> criteria = Criteria.of(CouponApplicableSku.class)
                .fields(CouponApplicableSku::getFskuId).andEqualTo(CouponApplicableSku::getFcouponId, coupon.getFcouponId());
        Result<List<CouponApplicableSku>> couponApplicableSkuResult = couponApplicableSkuApi.queryByCriteria(criteria);
        Ensure.that(couponApplicableSkuResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
        List<CouponApplicableSku> couponApplicableSkus = couponApplicableSkuResult.getData();
        if (CollectionUtil.isNotEmpty(couponApplicableSkus)) {
            List<String> skuIds = couponApplicableSkus.stream().map(CouponApplicableSku::getFskuId).map(String::valueOf).collect(toList());
            return skuIds;
        }
        return Lists.newArrayList();
    }

    /**
     * 根据优惠券id获取Alias名称
     * @param fcouponId
     * @return
     */
    private String getCouponAliasName(Long fcouponId) {
        if (Objects.isNull(fcouponId)) {
            throw new IllegalArgumentException("优惠券id不能为空");
        }
        return COUPON_ALIAS_PREFIX + fcouponId;
    }

    @Override
    public Result<SearchItemListVo<SearchItemVo>> searchSkuList(SearchItemDto searchItemDto) {
        if (!StringUtils.isEmpty(searchItemDto.getSearchFullText())) {
            searchRecordService.insertSearchRecordAsync(searchItemDto.getSearchFullText(), searchItemDto.getFuid());
        }
        // 初始化PageVo
        SearchItemListVo<SearchItemVo> pageVo = new SearchItemListVo<>();
        pageVo.setIsLogin(searchItemDto.getIsLogin());
        pageVo.setTotalCount(0);
        pageVo.setPageSize(1);

        this.setCategoryCondition(searchItemDto);
        EsCriteria criteria = EsCriteria.build(searchItemDto);
        if (searchItemDto.getCouponId() != null) {
            criteria.setIndexName(this.getCouponAliasName(Long.parseLong(String.valueOf(searchItemDto.getCouponId()))));
        }
        this.setSearchCondition(searchItemDto, criteria);
        String soldAmountScript = "1-Math.pow(doc['fsell_total'].value + 1, -1)";
        Map<String, Object> resultMap = esManager.functionQueryForResponse(criteria, soldAmountScript, CombineFunction.SUM);

        List<Map<String, Object>> resultList = (List<Map<String, Object>>) resultMap.get("resultList");
        List<SearchItemVo> voList = new LinkedList<>();
        pageVo.setList(voList);
        Map<String, Object> baseInfoMap = (Map<String, Object>) resultMap.get("baseInfoMap");
        //价格名称
        String priceName = this.getUserPriceType(searchItemDto);
        if (!CollectionUtils.isEmpty(resultList)) {
            for (Map<String, Object> map : resultList) {
                SearchItemVo vo = new SearchItemVo();
                if (map.get("fskuId") != null) {
                    vo.setFskuId(Integer.parseInt(String.valueOf(map.get("fskuId"))));
                }
                if (map.get("fskuName") != null) {
                    vo.setFskuName(String.valueOf(map.get("fskuName")));
                }
                if (map.get("ftradeId") != null) {
                    vo.setFtradedId(Integer.parseInt(String.valueOf(map.get("ftradeId"))));
                }
                if (map.get("ftradeName") != null) {
                    vo.setFtradeName(String.valueOf(map.get("ftradeName")));
                }
                if (map.get("fsellTotal") != null) {
                    vo.setFsellNum(Long.parseLong(String.valueOf(map.get("fsellTotal"))));
                }
                if (map.get("fskuThumbImage") != null) {
                    vo.setFimgUrl(String.valueOf(map.get("fskuThumbImage")));
                }
                if (map.get("fgoodsId") != null) {
                    vo.setFgoodsId(Integer.parseInt(String.valueOf(map.get("fgoodsId"))));
                }
                if (map.get("fskuStatus") != null) {
                    vo.setFskuStatus(Integer.parseInt(String.valueOf(map.get("fskuStatus"))));
                }
                if (map.get("flabelId") != null) {
                    vo.setFlabelId(Integer.parseInt(String.valueOf(map.get("flabelId"))));
                }
                if (map.get("fcouponIds") != null) {
                    List<Integer> fcouponIds = (List<Integer>) map.get("fcouponIds");
                    vo.setFcouponIds(fcouponIds);
                }
                if (map.get(priceName) != null && searchItemDto.getIsLogin()) {
                    Map<String, Object> priceMap = (Map<String, Object>) map.get(priceName);
                    if (priceMap.get("min_price") != null) {
                        BigDecimal min_price_penny = new BigDecimal(String.valueOf(priceMap.get("min_price")));
                        BigDecimal min_price_yuan = min_price_penny.divide(ONE_HUNDRED).setScale(2, BigDecimal.ROUND_HALF_UP);
                        vo.setFbatchSellPrice(min_price_yuan);
                    } else {
                        vo.setFbatchSellPrice(BigDecimal.ZERO);
                    }
                }
                if (map.get("fstockRemainNumTotal") != null) {
                    vo.setFremainTotal(Integer.parseInt(String.valueOf(map.get("fstockRemainNumTotal"))));
                }
                voList.add(vo);
            }
            pageVo.setPageSize(searchItemDto.getPageSize());
            pageVo.setCurrentPage(searchItemDto.getPageIndex());
            pageVo.setTotalCount(Integer.parseInt(String.valueOf(baseInfoMap.get("totalHits"))));
        }

        return Result.success(pageVo);
    }


    /**
     * 根据用户身份选择价格类型
     *
     * @param searchItemDto
     * @return
     */
    private String getUserPriceType(SearchItemDto searchItemDto) {
        //默认为未认证
        String fuserTypeId = "0";
        if (searchItemDto.getIsLogin() && searchItemDto.getFuid() != null) {
            //若登录信息里面存的用户认证类型和认证状态可用，不用查库
            if (Objects.equals(UserVerifyStatusEnum.AUTHENTICATED.getCode(), searchItemDto.getFverifyStatus())) {
                fuserTypeId = String.valueOf(searchItemDto.getFoperateType());
            } else {
                Result<User> userResult = userApi.queryOneByCriteria(Criteria.of(User.class)
                        .fields(User::getFuid, User::getFoperateType, User::getFverifyStatus)
                        .andEqualTo(User::getFuid, searchItemDto.getFuid()));
                User user = ResultUtils.getData(userResult);
                if (userResult.getData() == null) {
                    throw new BizException(MallResultStatus.USER_NOT_EXIST);
                }
                if (Objects.equals(UserVerifyStatusEnum.AUTHENTICATED.getCode(), user.getFverifyStatus())) {
                    fuserTypeId = String.valueOf(user.getFoperateType());
                }
            }
        }
        String priceName = PRICE_TYPE_PREFIX_CAMEL + fuserTypeId;
        return priceName;
    }

    /**
     * 设定搜索条件
     *
     * @param searchItemDto
     * @param criteria
     */
    private void setSearchCondition(SearchItemDto searchItemDto, EsCriteria criteria) {
        if (searchItemDto.getSearchFullText() != null) {
            MatchQueryBuilder pinyinMatch = QueryBuilders.matchQuery("fsku_name.pinyin", searchItemDto.getSearchFullText());
            criteria.getFilterBuilder().should(pinyinMatch);
        }
        // 商品属性条件
        if (CollectionUtils.isNotEmpty(searchItemDto.getFattributeItemId())) {
            String fieldname = "attributes.fclass_attribute_item_id";
            DisMaxQueryBuilder disMaxQuerys = QueryBuilders.disMaxQuery();
            for (Object value : searchItemDto.getFattributeItemId()) {
                disMaxQuerys.add(QueryBuilders.termsQuery(fieldname, value));
            }
            criteria.getFilterBuilder().must(QueryBuilders.nestedQuery("attributes", disMaxQuerys, ScoreMode.None));
        }
        // 库存条件
        this.setStockCondition(searchItemDto, criteria);
        // 价格条件
        this.setPriceCondition(searchItemDto, criteria);
    }

    /**
     * 提取商品属性聚合信息
     *
     * @param attributeAggs
     * @return
     */
    private List<GoodsAttributeFilterVo> getGoodAttributeList(List<Map<String, Object>> attributeAggs) {
        List<GoodsAttributeFilterVo> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(attributeAggs)) {
            return resultList;
        }
        List<Map<String, Object>> aggList = (List<Map<String, Object>>) attributeAggs.get(0).get("attribute_id");
        resultList = EsBeanUtil.getValueObjectList(GoodsAttributeFilterVo.class, aggList);
        return resultList;
    }


    /**
     * 价格条件
     *
     * @param searchItemDto
     * @param criteria
     */
    private void setPriceCondition(SearchItemDto searchItemDto, EsCriteria criteria) {
        if (criteria == null) {
            return;
        }

        String priceFieldName = this.getUserPriceType(searchItemDto);
        String fieldName = humpToLine2(priceFieldName) + PRICE_TYPE_SUFFIX;
        if (searchItemDto.getPriceOrderBy() != null) {
            criteria.sortBy(fieldName, searchItemDto.getPriceOrderBy());
        }

        if (searchItemDto.getFpriceStart() != null) {
            BigDecimal startPrice_yuan = searchItemDto.getFpriceStart();
            BigDecimal startPrice_penny = startPrice_yuan.multiply(ONE_HUNDRED).setScale(0, BigDecimal.ROUND_HALF_UP);
            criteria.rangeFrom(fieldName, String.valueOf(startPrice_penny));
        }

        if (searchItemDto.getFpriceEnd() != null) {
            BigDecimal endPrice_yuan = searchItemDto.getFpriceEnd();
            BigDecimal endPrice_penny = endPrice_yuan.multiply(ONE_HUNDRED).setScale(0, BigDecimal.ROUND_HALF_UP);
            criteria.rangeTo(fieldName, String.valueOf(endPrice_penny));
        }
    }

    /**
     * 驼峰转下划线
     *
     * @param str
     * @return
     */
    public static String humpToLine2(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 库存条件
     *
     * @param criteria
     */
    private void setStockCondition(SearchItemDto searchItemDto, EsCriteria criteria) {
        if (criteria == null) {
            return;
        }

        // 是否仅显示有货
        if (searchItemDto.getIsStockNotEmpty() != null && searchItemDto.getIsStockNotEmpty() == 1) {
            criteria.rangeFrom("fstock_remain_num_total", 1);
        }

        // 无货商品置底
//        if (StringUtils.isNotEmpty(searchItemDto.getPriceOrderBy()) || StringUtils.isNotEmpty(searchItemDto.getSellAmountOrderBy())) {
        DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
        RangeQueryBuilder onOut = QueryBuilders.rangeQuery("fstock_remain_num_total").gt(0);
        TermQueryBuilder soldOut = QueryBuilders.termQuery("fstock_remain_num_total", 0).boost(Integer.MIN_VALUE);
        disMaxQueryBuilder.add(onOut).add(soldOut);
        criteria.getFilterBuilder().must(disMaxQueryBuilder);
//        }
    }

    /**
     * 添加聚合条件
     *
     * @param criteria
     */
    private void setAggregation(EsCriteria criteria) {
        if (criteria == null) {
            return;
        }
        criteria.termAggregate("fcategory_id3", "fcategory_id3").subAggregate("fcategory_name3", "fcategory_name3.keyword");
        criteria.termAggregate("fbrand_id", "fbrand_id").subAggregate("fbrand_name", "fbrand_name.keyword");
        criteria.termAggregate("forigin_id", "forigin_id").subAggregate("forigin_name", "forigin_name.keyword");
        criteria.termAggregate("ftrade_id", "ftrade_id").subAggregate("ftrade_name", "ftrade_name.keyword");

        AggregationBuilder attributeAgg = AggregationBuilders.terms("attribute_id").field("attributes.fclass_attribute_id").order(BucketOrder.count(false))
                .subAggregation(AggregationBuilders.terms("attribute_name").field("attributes.fclass_attribute_name.keyword").order(BucketOrder.count(false))
                        .subAggregation(AggregationBuilders.terms("attribute_item_id").field("attributes.fclass_attribute_item_id").order(BucketOrder.count(false))
                                .subAggregation(AggregationBuilders.terms("attribute_item_value").field("attributes.fclass_attribute_item_val.keyword").order(BucketOrder.count(false)))));

        AggregationBuilder nestedAgg = AggregationBuilders.nested("attribute", "attributes").subAggregation(attributeAgg);
        criteria.getAggBuilders().put("attribute", nestedAgg);
    }


}
