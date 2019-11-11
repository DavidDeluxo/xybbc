package com.xingyun.bbc.mall.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.xingyun.bbc.core.market.api.CouponApi;
import com.xingyun.bbc.core.market.api.CouponApplicableSkuApi;
import com.xingyun.bbc.core.market.api.CouponApplicableSkuConditionApi;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.market.po.CouponApplicableSku;
import com.xingyun.bbc.core.market.po.CouponApplicableSkuCondition;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.core.utils.StringUtil;
import com.xingyun.bbc.mall.common.ensure.Ensure;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.dto.CouponGoodsDto;
import com.xingyun.bbc.mall.model.vo.SearchItemListVo;
import com.xingyun.bbc.mall.model.vo.SearchItemVo;
import com.xingyun.bbc.mall.service.CouponGoodsService;
import com.xingyun.bbc.mall.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hekaijin
 * @date 2019/11/11 11:36
 * @Description
 */
@Service
public class CouponGoodsServiceImpl implements CouponGoodsService {

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private CouponApi couponApi;
    @Autowired
    private CouponApplicableSkuApi couponApplicableSkuApi;
    @Autowired
    private CouponApplicableSkuConditionApi couponApplicableSkuConditionApi;


    @Override
    public Result<SearchItemListVo<SearchItemVo>> queryGoodsList(CouponGoodsDto dto) {

        Result<Coupon> couponResult = couponApi.queryById(dto.getCouponId());
        Ensure.that(couponResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);

        SearchItemListVo<SearchItemVo> listVo = new SearchItemListVo<>(0, dto.getPageIndex(), dto.getPageSize(), Lists.newArrayList());

        if (Objects.isNull(couponResult.getData())) return Result.success(listVo);

        Result<List<CouponApplicableSku>> skuListRes = couponApplicableSkuApi.queryByCriteria(Criteria.of(CouponApplicableSku.class).andEqualTo(CouponApplicableSku::getFcouponId, dto.getCouponId()));
        Ensure.that(skuListRes.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);

        //skuId
        if (!CollectionUtils.isEmpty(skuListRes.getData())) {
            dto.setFskuIds(skuListRes.getData().stream().map(CouponApplicableSku::getFskuId).collect(Collectors.toList()));
        }

        Result<List<CouponApplicableSkuCondition>> skuConRes = couponApplicableSkuConditionApi.queryByCriteria(Criteria.of(CouponApplicableSkuCondition.class).andEqualTo(CouponApplicableSkuCondition::getFcouponId, dto.getCouponId()));
        Ensure.that(skuConRes.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);

        List<CouponApplicableSkuCondition> skuConResData = skuConRes.getData();

        if (CollectionUtils.isEmpty(skuConResData)) return goodsService.searchSkuList(dto);

        //一级分类
        Set<Integer> categoryIdL1 = new HashSet<>();
        //二级分类
        Set<Integer> categoryIdL2 = new HashSet<>();
        //三级分类
        Set<Integer> categoryIdL3 = new HashSet<>();
        //品牌
        Set<Integer> brandId = new HashSet<>();
        //标签
        Set<Integer> labelId = new HashSet<>();
        //贸易类型
        Set<Integer> tradeId = new HashSet<>();

        skuConResData.forEach(sc -> {

            String categoryIdJson = sc.getFcategoryId();

            //解析多级分类
            if (StringUtil.isNotBlank(categoryIdJson)) {
                this.addCategoryIds(categoryIdL1, categoryIdL2, categoryIdL3, categoryIdJson);
            }

            this.addTypes(brandId, labelId, tradeId, sc);

        });

        if (categoryIdL1.size() > 0) dto.setFcategoryIdL1((List<Integer>) categoryIdL1);

        if (categoryIdL2.size() > 0) dto.setFcategoryIdL2((List<Integer>) categoryIdL2);

        if (categoryIdL3.size() > 0) dto.setFcategoryId((List<Integer>) categoryIdL3);

        if (brandId.size() > 0) dto.setFbrandId((List<Integer>) brandId);

        if (labelId.size() > 0) dto.setFlabelId((List<Integer>) labelId);

        if (tradeId.size() > 0) dto.setFtradeId((List<Integer>) tradeId);

        return goodsService.searchSkuList(dto);
    }

    private void addTypes(Set<Integer> brandId, Set<Integer> labelId, Set<Integer> tradeId, CouponApplicableSkuCondition sc) {
        String fbrandId = sc.getFbrandId();
        String flabelId = sc.getFlabelId();
        String ftradeCode = sc.getFtradeCode();

        if (StringUtil.isNotBlank(fbrandId)) {
            JSONArray brands = JSON.parseArray(fbrandId);
            brands.forEach(b -> brandId.add((Integer) b));
        }
        if (StringUtil.isNotBlank(flabelId)) {
            JSONArray labelIds = JSON.parseArray(flabelId);
            labelIds.forEach(b -> labelId.add((Integer) b));
        }
        if (StringUtil.isNotBlank(ftradeCode)) {
            JSONArray tradeCodes = JSON.parseArray(ftradeCode);
            tradeCodes.forEach(b -> tradeId.add((Integer) b));
        }
    }


    private void addCategoryIds(Set<Integer> categoryIdL1, Set<Integer> categoryIdL2, Set<Integer> categoryIdL3, String categoryIdJson) {
        JSONObject js = JSON.parseObject(categoryIdJson);
        if (js.isEmpty()) return;

        js.keySet().forEach(key -> {

            int keys = Integer.parseInt(key + "");
            if (keys == 1) {
                js.getJSONArray(key).forEach(v -> {
                    if (!StringUtil.isBlank(v + "")) categoryIdL1.add(Integer.valueOf(v + ""));
                });

            } else if (keys == 2) {
                js.getJSONArray(key).forEach(v -> {
                    if (!StringUtil.isBlank(v + "")) categoryIdL2.add(Integer.valueOf(v + ""));
                });

            } else if (keys == 3) {
                js.getJSONArray(key).forEach(v -> {
                    if (!StringUtil.isBlank(v + "")) categoryIdL3.add(Integer.valueOf(v + ""));
                });
            }

        });
    }


}
