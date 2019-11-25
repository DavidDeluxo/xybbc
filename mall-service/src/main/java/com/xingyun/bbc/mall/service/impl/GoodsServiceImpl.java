package com.xingyun.bbc.mall.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.google.common.collect.Sets;
import com.xingyun.bbc.common.elasticsearch.config.EsBeanUtil;
import com.xingyun.bbc.common.elasticsearch.config.EsCriteria;
import com.xingyun.bbc.common.elasticsearch.config.EsManager;
import com.xingyun.bbc.core.activity.model.dto.CouponQueryDto;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.market.api.CouponApi;
import com.xingyun.bbc.core.market.api.CouponApplicableSkuApi;
import com.xingyun.bbc.core.market.api.CouponApplicableSkuConditionApi;
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
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.enums.MallResultStatus;
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

//    @Autowired
//    MallSkuSearchApi mallSkuSearchApi;

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

    private void setCategoryCondition(SearchItemDto searchItemDto) {
        // 商品类目条件
        if (CollectionUtils.isNotEmpty(searchItemDto.getFUnicategoryIds())
                && searchItemDto.getFcategoryLevel() != null) {
            Integer fcateogryLevel = searchItemDto.getFcategoryLevel();
            if (fcateogryLevel == 1) {
                searchItemDto.setFcategoryIdL1(searchItemDto.getFUnicategoryIds());
            }
            if (fcateogryLevel == 2) {
                searchItemDto.setFcategoryIdL2(searchItemDto.getFUnicategoryIds());
            }
            if (fcateogryLevel == 3) {
                searchItemDto.setFcategoryId(searchItemDto.getFUnicategoryIds());
            }
        }
    }

    private SkuIdVo queryApplicableSku(Long fcouponId, Integer pageIndex, Integer pageSize){
        pageIndex = pageIndex == null ? 1 : pageIndex;
        pageSize = pageSize == null ? 100 : pageSize;
        SkuIdVo skuIdVo = new SkuIdVo();
        skuIdVo.setPageIndex(pageIndex);
        skuIdVo.setPageSize(pageSize);
        skuIdVo.setTotalCount(0);
        skuIdVo.setFskuIds(Lists.newArrayList());

        // 先查 t_bbc_coupon_assign_sku 再查 t_bbc_coupon_assign_sku_condition 这俩张表
        Result<Coupon> couponResult = couponApi.queryById(fcouponId);
        Ensure.that(couponResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
        Ensure.that(couponResult.getData()).isNotNull(MallExceptionCode.COUPON_IS_NOT_EXIST);
        Coupon coupon = couponResult.getData();
        //全选
        if(coupon.getFapplicableSku() == 1){
            Criteria skuCriteria = Criteria.of(GoodsSku.class).fields(GoodsSku::getFskuId);
            Result<Integer> totalCountResult = goodsSkuApi.countByCriteria(skuCriteria);
            Ensure.that(totalCountResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
            Result<List<GoodsSku>> goodsSkuResult  = goodsSkuApi.queryByCriteria(skuCriteria.page(pageIndex, pageSize));
            Ensure.that(goodsSkuResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
            skuIdVo.setTotalCount(totalCountResult.getData());
            List<String> SkuIdString = goodsSkuResult.getData().stream().map(sku -> String.valueOf(sku.getFskuId())).collect(Collectors.toList());
            skuIdVo.setFskuIds(SkuIdString);
            return skuIdVo;
        }
        //正选
        if(coupon.getFapplicableSku() == 2){
            Criteria<CouponApplicableSku, Object> criteria = Criteria.of(CouponApplicableSku.class)
                    .fields(CouponApplicableSku::getFskuId).andEqualTo(CouponApplicableSku::getFcouponId, fcouponId);
            Result<List<CouponApplicableSku>> couponApplicableSkuResult = couponApplicableSkuApi.queryByCriteria(criteria.page(pageIndex, pageSize));
            Ensure.that(couponApplicableSkuResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
            List<CouponApplicableSku> couponApplicableSkus = couponApplicableSkuResult.getData();
            if (CollectionUtil.isNotEmpty(couponApplicableSkus)) {
                List<Long> skuIds = couponApplicableSkus.stream().map(CouponApplicableSku::getFskuId).collect(toList());
                List<String> SkuIdString = skuIds.stream().map(skuId -> String.valueOf(skuId)).collect(Collectors.toList());
                skuIdVo.setFskuIds(SkuIdString);
                Result<Integer> totalCountResult = couponApplicableSkuApi.countByCriteria(criteria);
                Ensure.that(totalCountResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
                skuIdVo.setTotalCount(totalCountResult.getData());
                return skuIdVo;
            }
            // 再查 t_bbc_coupon_assign_sku_condition
            Result<List<CouponApplicableSkuCondition>> conditionResult = couponApplicableSkuConditionApi.queryByCriteria(Criteria.of(CouponApplicableSkuCondition.class)
                    .andEqualTo(CouponApplicableSkuCondition::getFcouponId,fcouponId));
            Ensure.that(conditionResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
            List<CouponApplicableSkuCondition> conditionList = conditionResult.getData();
            if(CollectionUtils.isNotEmpty(conditionList)){
                List<CouponSkuQueryDto> couponSkuQueryDtos = Lists.newArrayList();
                conditionList.stream().forEach(couponApplicableSkuCondition -> {
                    //对应字段转成java object
                    CouponSkuQueryDto couponSkuQueryDto = new CouponSkuQueryDto();
                    parseJsonAndSetFields(couponSkuQueryDto, couponApplicableSkuCondition);
                    couponSkuQueryDtos.add(couponSkuQueryDto);
                });
                return querySkuByCondition(couponSkuQueryDtos, pageIndex, pageSize, skuIdVo);
            }
            return skuIdVo;
        }
        //反选
        if(coupon.getFapplicableSku() == 3){
            Criteria<CouponApplicableSku, Object> criteria = Criteria.of(CouponApplicableSku.class)
                    .fields(CouponApplicableSku::getFskuId).andEqualTo(CouponApplicableSku::getFcouponId, fcouponId);
            Result<List<CouponApplicableSku>> couponApplicableSkuResult = couponApplicableSkuApi.queryByCriteria(criteria);
            Ensure.that(couponApplicableSkuResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
            List<CouponApplicableSku> couponApplicableSkus = couponApplicableSkuResult.getData();
            if (CollectionUtil.isNotEmpty(couponApplicableSkus)) {
                List<Long> skuIds = couponApplicableSkus.stream().map(CouponApplicableSku::getFskuId).collect(toList());
                Criteria<GoodsSku, Object> skuIdCriteria = Criteria.of(GoodsSku.class).andNotIn(GoodsSku::getFskuId, skuIds);
                Result<List<GoodsSku>> skuResult = goodsSkuApi.queryByCriteria(skuIdCriteria.fields(GoodsSku::getFskuId).page(pageIndex, pageSize));
                Ensure.that(skuResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
                List<String> SkuIdString = skuResult.getData().stream().map(sku -> String.valueOf(sku.getFskuId())).collect(Collectors.toList());
                skuIdVo.setFskuIds(SkuIdString);
                Result<Integer> totalCountResult = goodsSkuApi.countByCriteria(skuIdCriteria);
                Ensure.that(totalCountResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
                skuIdVo.setTotalCount(totalCountResult.getData());
                return skuIdVo;
            }
            // 再查 t_bbc_coupon_assign_sku_
            // 再查 t_bbc_coupon_assign_sku_condition
            Result<List<CouponApplicableSkuCondition>> conditionResult = couponApplicableSkuConditionApi.queryByCriteria(Criteria.of(CouponApplicableSkuCondition.class)
                    .andEqualTo(CouponApplicableSkuCondition::getFcouponId,fcouponId));
            Ensure.that(conditionResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
            List<CouponApplicableSkuCondition> conditionList = conditionResult.getData();
            if(CollectionUtils.isNotEmpty(conditionList)){
                List<CouponSkuQueryDto> couponSkuQueryDtos = Lists.newArrayList();
                conditionList.stream().forEach(couponApplicableSkuCondition -> {
                    //对应字段转成java object
                    CouponSkuQueryDto couponSkuQueryDto = new CouponSkuQueryDto();
                    parseJsonAndSetFields(couponSkuQueryDto, couponApplicableSkuCondition);
                    couponSkuQueryDtos.add(couponSkuQueryDto);
                });
                return querySkuByConditionReverse(couponSkuQueryDtos, pageIndex, pageSize, skuIdVo);
            }
            return skuIdVo;
        }
        return skuIdVo;
    }




    /**
     * @author
     * @date 2019-11-14
     * @description :  根据优惠券条件查商品
     * @version 1.0.0
     */
    private SkuIdVo querySkuByCondition(List<CouponSkuQueryDto> couponSkuQueryDtos, Integer pageIndex, Integer pageSize, SkuIdVo skuIdVo){
        Criteria<GoodsSku, Object> skuObjectCriteria = Criteria.of(GoodsSku.class)
                .fields(GoodsSku::getFskuId,
                        GoodsSku::getFgoodsId,
                        GoodsSku::getFskuName,
                        GoodsSku::getFskuCode,
                        GoodsSku::getFbrandName,
                        GoodsSku::getFcategoryName3);
        couponSkuQueryDtos.forEach(skuQueryDto -> {
            List<Long> oneLevel = Lists.newArrayList();
            List<Long> twoLevel = Lists.newArrayList();
            List<Long> threeLevel = Lists.newArrayList();
            if(skuQueryDto.getCategoryIds() != null){
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
            // 单组条件开始
            skuObjectCriteria.orLeft();
            // 分类间或关系开始
            skuObjectCriteria.orLeft();
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(oneLevel)) {
                skuObjectCriteria.orIn(GoodsSku::getFcategoryId1, oneLevel);
            }
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(twoLevel)) {
                skuObjectCriteria.orIn(GoodsSku::getFcategoryId2, twoLevel);
            }
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(threeLevel)) {
                skuObjectCriteria.orIn(GoodsSku::getFcategoryId3, threeLevel);
            }
            // 分类间或关系结束
            skuObjectCriteria.addRight();
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(brandIds)) {
                skuObjectCriteria.orIn(GoodsSku::getFbrandId, brandIds);
            }
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(labelIds)) {
                skuObjectCriteria.orIn(GoodsSku::getFlabelId, labelIds);
            }
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(goodsIds)) {
                skuObjectCriteria.orIn(GoodsSku::getFgoodsId, goodsIds);
            }
            // 单组条件结束
            skuObjectCriteria.addRight();
        });
        Result<List<GoodsSku>> goodsSkuResult = goodsSkuApi.queryByCriteria(skuObjectCriteria.page(pageIndex, pageSize));
        if (!goodsSkuResult.isSuccess()) {
            throw new BizException(ResultStatus.NOT_IMPLEMENTED);
        }
        List<GoodsSku> goodsSkus = goodsSkuResult.getData();
        List<String> skuIds = goodsSkus.stream().map(sku->String.valueOf(sku.getFskuId())).collect(Collectors.toList());
        skuIdVo.setFskuIds(skuIds);
        Result<Integer> totalCountResult =  goodsSkuApi.countByCriteria(skuObjectCriteria);
        Ensure.that(totalCountResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
        skuIdVo.setTotalCount(totalCountResult.getData());
        return skuIdVo;
    }


    private SkuIdVo querySkuByConditionReverse(List<CouponSkuQueryDto> couponSkuQueryDtos, Integer pageIndex, Integer pageSize, SkuIdVo skuIdVo){
        Criteria<GoodsSku, Object> skuObjectCriteria = Criteria.of(GoodsSku.class)
                .fields(GoodsSku::getFskuId,
                        GoodsSku::getFgoodsId,
                        GoodsSku::getFskuName,
                        GoodsSku::getFskuCode,
                        GoodsSku::getFbrandName,
                        GoodsSku::getFcategoryName3);
        couponSkuQueryDtos.forEach(skuQueryDto -> {
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
            // 单组条件开始
            skuObjectCriteria.andLeft();
            // 分类间或关系开始
            skuObjectCriteria.andLeft();
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(oneLevel)) {
                skuObjectCriteria.andNotIn(GoodsSku::getFcategoryId1, oneLevel);
            }
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(twoLevel)) {
                skuObjectCriteria.andNotIn(GoodsSku::getFcategoryId2, twoLevel);
            }
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(threeLevel)) {
                skuObjectCriteria.andNotIn(GoodsSku::getFcategoryId3, threeLevel);
            }
            // 分类间或关系结束
            skuObjectCriteria.addRight();
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(brandIds)) {
                skuObjectCriteria.andNotIn(GoodsSku::getFbrandId, brandIds);
            }
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(labelIds)) {
                skuObjectCriteria.andNotIn(GoodsSku::getFlabelId, labelIds);
            }
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(goodsIds)) {
                skuObjectCriteria.andNotIn(GoodsSku::getFgoodsId, goodsIds);
            }
            // 单组条件结束
            skuObjectCriteria.addRight();
        });
        Result<List<GoodsSku>> goodsSkuResult = goodsSkuApi.queryByCriteria(skuObjectCriteria.page(pageIndex, pageSize));
        if (!goodsSkuResult.isSuccess()) {
            throw new BizException(ResultStatus.NOT_IMPLEMENTED);
        }
        List<GoodsSku> goodsSkus = goodsSkuResult.getData();
        List<String> skuIds = goodsSkus.stream().map(sku->String.valueOf(sku.getFskuId())).collect(Collectors.toList());
        skuIdVo.setFskuIds(skuIds);
        Result<Integer> totalCountResult =  goodsSkuApi.countByCriteria(skuObjectCriteria);
        Ensure.that(totalCountResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
        skuIdVo.setTotalCount(totalCountResult.getData());
        return skuIdVo;
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
    public Result<Boolean> updateCouponList(){
        EsCriteria criteria = new EsCriteria();
        List<Integer> couponIds = com.google.common.collect.Lists.newArrayList();
        couponIds.add(2);
//        for(int i = 1; i<=100; i++){
//            couponIds.add(i);
//        }
        Map<String, Object> updateParam = new HashMap<>();
        updateParam.put("fcoupon_ids", 2);
        criteria.addUpdateRequest("55719", updateParam);
        try {
            esManager.updateInBulk(criteria);
        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.success(true);
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
    public void updateCouponIdForAllSku(RefreshCouponDto refreshCouponDto){
        Integer pageSize = 2000;
        EsCriteria criteria = EsCriteria.build(null);
//        DisMaxQueryBuilder disMaxQueryBuilder = new DisMaxQueryBuilder();

        criteria.setPageSize(pageSize);
        Map<String, Object> resultMap = esManager.queryWithBaseInfo(criteria);
        if(resultMap.get("baseInfoMap") != null){
            Map<String, Object> baseInfoMap = (Map<String, Object>) resultMap.get("baseInfoMap");
            Integer totalPage = 0;
            if(baseInfoMap.get("totalPage") != null){
                totalPage = Integer.parseInt(String.valueOf(baseInfoMap.get("totalPage")));
            }
            for(int var = 1; var <= totalPage; var ++){
                log.info("处理第{}页, 共{}页开始", var, totalPage);
                criteria.setPageIndex(var);
                Map<String, Object> aresultMap = esManager.queryWithBaseInfo(criteria);
                List<Map<String, Object>> aList = (List<Map<String, Object>>) aresultMap.get("resultList");
                Map<String, Map<String, Object>> updateMap = new HashMap<>();
                for(Map<String, Object> sourceMap : aList){
                    if(sourceMap.get("fsku_id") != null){
                        String fskuId = String.valueOf(sourceMap.get("fsku_id"));
                        List<Integer> couponList = getCouponListForSku(fskuId);
                        sourceMap.put("fcoupon_ids",couponList);
                        updateMap.put(fskuId, sourceMap);
                    }
                }
                try {
                    esManager.indexInBulk(updateMap);
                }catch (Exception e){
                    e.printStackTrace();
                }
                log.info("处理第{}页, 共{}页结束", var, totalPage);
            }
        }
    }

    @Override
    public void deleteCouponInfoFromEsSku(Coupon coupon){
        if(coupon == null || coupon.getFcouponId() == null){
            return;
        }
        Long fcouponId = coupon.getFcouponId();
        long startTimeTotal = System.currentTimeMillis();
        int pageSize = 2000;
        SkuIdVo skuIdVo = queryApplicableSku(fcouponId, 1,9);
        if(skuIdVo.getTotalCount() == 0){
            return;
        }
        int totalPage = 0;
        if(skuIdVo.getTotalCount() % pageSize == 0){
            totalPage = skuIdVo.getTotalCount() / pageSize;
        }else{
            totalPage = skuIdVo.getTotalCount() / pageSize + 1;
        }
        for(int i = 1; i<=totalPage; i++){
            skuIdVo = queryApplicableSku(fcouponId, i,pageSize);
            List<String> skuIds = skuIdVo.getFskuIds();
            try {
                long startTime = System.currentTimeMillis();
                Map<String, Map<String,Object>> multiSourceMap = new HashMap<>();
                List<Map<String, Object>> responseList = esManager.getInBulk(skuIds);
//                log.info(JSON.toJSONString(responseList));
                for(Map<String, Object> sourceMap : responseList){
                    if(MapUtils.isNotEmpty(sourceMap)){
                        List<Integer> couponIdList = (List<Integer>) sourceMap.get("fcoupon_ids");
                        Set<Integer> set = Sets.newHashSet(couponIdList);
                        if(couponIdList.contains(fcouponId.intValue())){
                            set.remove(fcouponId.intValue());
                        }
                        sourceMap.put("fcoupon_ids", set);
                        multiSourceMap.put(String.valueOf(sourceMap.get("fsku_id")), sourceMap);
                    }
                }
                long endTime = System.currentTimeMillis();
                float excTime = (float) (endTime-startTime) / 1000;
                log.info("get_exec_time:{}, pagesize:{}, pageIndex:{}, totalPages:{}", excTime, pageSize, i, totalPage);

                long startTimeIndex = System.currentTimeMillis();
                if(MapUtils.isNotEmpty(multiSourceMap)){
                    BulkResponse bulkResponse = esManager.indexInBulk(multiSourceMap);
                }else {
                    log.info("multisourceMap is empty!");
                }
                long endTimeIndex = System.currentTimeMillis();
                float excTime_index = (float) (endTimeIndex-startTimeIndex) / 1000;
                log.info("index_exec_time:{}, pagesize:{}, pageIndex:{}, totalPages:{}", excTime_index, pageSize, i, totalPage);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        long endTimeTotal = System.currentTimeMillis();
        float excTimeTotal = (float) (endTimeTotal-startTimeTotal) / 1000;
        log.info("total_exec_time:{}, totalPages:{}", excTimeTotal, totalPage);
    }

    @Override
    public void updateEsSkuWithSkuUpdate(Map<String, Object> skuSourceMap){
        if(MapUtils.isEmpty(skuSourceMap)){
            return;
        }
        String fskuId = String.valueOf(skuSourceMap.get("fsku_id"));
        try {
            GetResponse getResponse = esManager.getSourceById(fskuId);
            if(getResponse.isExists()){
                log.info("更新sku:{}",fskuId);
                Map<String, Object> oldSourceMap = getResponse.getSourceAsMap();
                if(oldSourceMap.get("fcoupon_ids") != null){
                    List<Integer> couponIdList = (List<Integer>) oldSourceMap.get("fcoupon_ids");
                    log.info("保留现有优惠券id列表:{}", couponIdList);
                    skuSourceMap.put("fcoupon_ids", couponIdList);
                }
            }else {
                log.info("新增sku:{}",fskuId);
                List<Integer> couponList = getCouponListForSku(fskuId);
                log.info("新建sku对应优惠券id列表:{}", couponList);
                skuSourceMap.put("fcoupon_ids", couponList);
            }
            Map<String, Map<String, Object>> indexMap = new HashMap<>();
            indexMap.put(fskuId, skuSourceMap);
            esManager.indexInBulk(indexMap);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private List<Integer> getCouponListForSku(String fskuId){
        CouponQueryDto couponQueryDto = new CouponQueryDto();
        couponQueryDto.setSkuId(Long.parseLong(fskuId));
        List<Coupon> couponList = couponService.queryBySkuId(couponQueryDto);
        if(CollectionUtils.isEmpty(couponList)){
            return Lists.newArrayList();
        }
        List<Integer> couponIdList = couponList.stream().map(coupon -> coupon.getFcouponId().intValue()).collect(Collectors.toList());
        return couponIdList;
    }


    @Override
    public void updateEsSkuWithCouponInfo(Coupon coupon){
        if(coupon == null || coupon.getFcouponId() == null){
            return;
        }
        Long fcouponId = coupon.getFcouponId();
        long startTimeTotal = System.currentTimeMillis();
        int pageSize = 2000;
        SkuIdVo skuIdVo = queryApplicableSku(fcouponId, 1,9);
        if(skuIdVo.getTotalCount() == 0){
            return;
        }
        int totalPage = 0;
        if(skuIdVo.getTotalCount() % pageSize == 0){
            totalPage = skuIdVo.getTotalCount() / pageSize;
        }else{
            totalPage = skuIdVo.getTotalCount() / pageSize + 1;
        }
        for(int i = 1; i<=totalPage; i++){
            skuIdVo = queryApplicableSku(fcouponId, i,pageSize);
            List<String> skuIds = skuIdVo.getFskuIds();
            try {
                long startTime = System.currentTimeMillis();
                Map<String, Map<String,Object>> multiSourceMap = new HashMap<>();
                List<Map<String, Object>> responseList = esManager.getInBulk(skuIds);
//                log.info(JSON.toJSONString(responseList));
                for(Map<String, Object> sourceMap : responseList){
                    if(MapUtils.isNotEmpty(sourceMap)){
                        List<Integer> couponIdList = (List<Integer>) sourceMap.get("fcoupon_ids");
                        if(!couponIdList.contains(fcouponId.intValue())){
                            couponIdList.add(fcouponId.intValue());
                        }
                        sourceMap.put("fcoupon_ids", couponIdList);
                        multiSourceMap.put(String.valueOf(sourceMap.get("fsku_id")), sourceMap);
                    }
                }
                long endTime = System.currentTimeMillis();
                float excTime = (float) (endTime-startTime) / 1000;
                log.info("get_exec_time:{}, pagesize:{}, pageIndex:{}, totalPages:{}", excTime, pageSize, i, totalPage);

                long startTimeIndex = System.currentTimeMillis();
                if(MapUtils.isNotEmpty(multiSourceMap)){
                    BulkResponse bulkResponse = esManager.indexInBulk(multiSourceMap);
                }else {
                    log.info("multisourceMap is empty!");
                }
                long endTimeIndex = System.currentTimeMillis();
                float excTime_index = (float) (endTimeIndex-startTimeIndex) / 1000;
                log.info("index_exec_time:{}, pagesize:{}, pageIndex:{}, totalPages:{}", excTime_index, pageSize, i, totalPage);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        long endTimeTotal = System.currentTimeMillis();
        float excTimeTotal = (float) (endTimeTotal-startTimeTotal) / 1000;
        log.info("total_exec_time:{}, totalPages:{}", excTimeTotal, totalPage);
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
        this.setSearchCondition(searchItemDto, criteria);
        String soldAmountScript = "1-Math.pow(doc['fsell_total'].value + 1, -1)";

        Map<String, Object> resultMap = esManager.functionQueryForResponse(criteria, soldAmountScript, CombineFunction.SUM);
        List<Map<String, Object>> resultList = (List<Map<String, Object>>) resultMap.get("resultList");
        List<SearchItemVo> voList = new LinkedList<>();
        pageVo.setList(voList);
        Map<String, Object> baseInfoMap = (Map<String, Object>) resultMap.get("baseInfoMap");
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
                if(map.get("fcouponIds") != null){
                    List<Integer> fcouponIds = (List<Integer>) map.get("fcouponIds");
                    vo.setFcouponIds(fcouponIds);
                }
                String priceName = this.getUserPriceType(searchItemDto);
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
            Result<User> userResult = userApi.queryOneByCriteria(Criteria.of(User.class).andEqualTo(User::getFuid, searchItemDto.getFuid()));
            if (!userResult.isSuccess()) {
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            if (userResult.getData() == null) {
                throw new BizException(MallResultStatus.USER_NOT_EXIST);
            }
            User user = userResult.getData();
            fuserTypeId = String.valueOf(user.getFoperateType());
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
        if (StringUtils.isNotEmpty(searchItemDto.getPriceOrderBy()) || StringUtils.isNotEmpty(searchItemDto.getSellAmountOrderBy())) {
            DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
            RangeQueryBuilder onOut = QueryBuilders.rangeQuery("fstock_remain_num_total").gt(0);
            TermQueryBuilder soldOut = QueryBuilders.termQuery("fstock_remain_num_total", 0).boost(Integer.MIN_VALUE);
            disMaxQueryBuilder.add(onOut).add(soldOut);
            criteria.getFilterBuilder().must(disMaxQueryBuilder);
        }
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
