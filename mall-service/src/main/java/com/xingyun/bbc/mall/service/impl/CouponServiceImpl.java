package com.xingyun.bbc.mall.service.impl;

import com.google.common.collect.Lists;
import com.xingyun.bbc.activity.model.dto.CouponQueryDto;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.market.api.CouponApi;
import com.xingyun.bbc.core.market.api.CouponApplicableSkuApi;
import com.xingyun.bbc.core.market.api.CouponApplicableSkuConditionApi;
import com.xingyun.bbc.core.market.enums.CouponApplicableSkuEnum;
import com.xingyun.bbc.core.market.enums.CouponStatusEnum;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.market.po.CouponApplicableSku;
import com.xingyun.bbc.core.market.po.CouponApplicableSkuCondition;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.GoodsApi;
import com.xingyun.bbc.core.sku.api.GoodsSkuApi;
import com.xingyun.bbc.core.sku.po.Goods;
import com.xingyun.bbc.core.sku.po.GoodsSku;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.JacksonUtils;
import com.xingyun.bbc.mall.base.utils.ResultUtils;
import com.xingyun.bbc.mall.common.ensure.Ensure;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CouponServiceImpl implements CouponService {

    /**
     * 1级分类
     */
    private static final String CATE_ONE = "1";
    /**
     * 2级分类
     */
    private static final String CATE_TWO = "2";
    /**
     * 3级分类
     */
    private static final String CATE_THREE = "3";

    /**
     * sku条件判断数量
     */
    private static final int SKU_CONDITION_NUM = 4;

    @Resource
    private CouponApi couponApi;

    @Resource
    private CouponApplicableSkuApi applicableSkuApi;

    @Resource
    private CouponApplicableSkuConditionApi applicableSkuConditionApi;

    @Resource
    private GoodsApi goodsApi;

    @Resource
    private GoodsSkuApi goodsSkuApi;


    @Override
    public List<Coupon> queryBySkuId(CouponQueryDto couponQueryDto) {
        Long skuId = couponQueryDto.getSkuId();
        // 获取所有优惠券
        List<Coupon> coupons = getCoupons(couponQueryDto);
        if (CollectionUtils.isEmpty(coupons)) {
            return Lists.newArrayList();
        }
        //符合SKU条件的优惠券配置
        List<Coupon> returnCoupons = new ArrayList<>();
        //需要进一步校验的优惠券配置
        List<Coupon> needToJudgeCoupons = new ArrayList<>();
        //校验优惠券配置,填充符合条件的优惠券配置，填充需要进一步校验的优惠券配置
        skuCheckAndFirstFilter(coupons, returnCoupons, needToJudgeCoupons);

        //从需要进一步校验的优惠券配置中筛选出符合条件的优惠券配置 填充到返回list中
        getSkuDataFromNeedToJudgeCoupons(returnCoupons, needToJudgeCoupons, skuId);

        if (CollectionUtils.isEmpty(returnCoupons)) {
            return Lists.newArrayList();
        }
        return returnCoupons;
    }

    /**
     * 根据条件获取所有优惠券
     *
     * @param couponQueryDto
     * @return
     */
    private List<Coupon> getCoupons(CouponQueryDto couponQueryDto) {
        // 优惠券状态
//        final List<Integer> couponStatus = Arrays.asList(CouponStatusEnum.PUSHED.getCode(), CouponStatusEnum.FINISHED.getCode());
        final List<Integer> couponStatus = Arrays.asList(CouponStatusEnum.PUSHED.getCode());
        Criteria<Coupon, Object> couponObjectCriteria = Criteria.of(Coupon.class).andIn(Coupon::getFcouponStatus, couponStatus);
        // 优惠券类型，1满减券、2折扣券
        List<Integer> couponTypes = couponQueryDto.getCouponTypes();
        if (CollectionUtils.isNotEmpty(couponTypes)) {
            couponObjectCriteria.andIn(Coupon::getFcouponType, couponTypes);
        }
        // 发放类型，1系统赠送、2页面领取、3新人注册、4会员认证、5首单完成、6订单满赠、7好友邀请、8券码激活
        List<Integer> releaseTypes = couponQueryDto.getReleaseTypes();
        if (CollectionUtils.isNotEmpty(releaseTypes)) {
            couponObjectCriteria.andIn(Coupon::getFreleaseType, releaseTypes);
        }
        Result<List<Coupon>> couponResult = couponApi.queryByCriteria(couponObjectCriteria);
        Ensure.that(couponResult.isSuccess()).isTrue(MallExceptionCode.SYSTEM_ERROR);
        return couponResult.getData();
    }


    private void skuCheckAndFirstFilter(List<Coupon> coupons, List<Coupon> returnCoupons, List<Coupon> needToJudgeCoupons) {
        for (Coupon coupon : coupons) {
            if (Objects.equals(CouponApplicableSkuEnum.ALL.getCode(), coupon.getFapplicableSku())) {
                returnCoupons.add(coupon);
                continue;
            }
            needToJudgeCoupons.add(coupon);
        }
    }


    /**
     * 从需要进一步校验的优惠券配置中筛选出符合条件的优惠券配置 填充到返回list中
     *
     * @param returnCoupons
     * @param needToJudgeCoupons
     * @param skuId
     */
    private void getSkuDataFromNeedToJudgeCoupons(List<Coupon> returnCoupons, List<Coupon> needToJudgeCoupons, Long skuId) {
        if (CollectionUtils.isEmpty(needToJudgeCoupons)) {
            return;
        }
        GoodsSku sku = ResultUtils.getDataNotNull(goodsSkuApi.queryById(skuId));
        Goods goods = ResultUtils.getDataNotNull(goodsApi.queryById(sku.getFgoodsId()));
        //适用商品可使用优惠券，1全部商品、2指定商品可用、3指定商品不可用，这里1的在第一步已经处理了，这里分组处理2和3两种情况
        Map<Integer, List<Coupon>> applicableSkuMap = needToJudgeCoupons.stream().collect(Collectors.groupingBy((Coupon::getFapplicableSku)));
        //2指定商品可用
        List<Coupon> applicableCoupons = applicableSkuMap.get(CouponApplicableSkuEnum.SOME.getCode());
        //筛选：指定可用,填充到返回数据中
        dealApplicableCoupons(returnCoupons, applicableCoupons, goods, sku);

        //3指定商品不可用
        List<Coupon> notApplicableCoupons = applicableSkuMap.get(CouponApplicableSkuEnum.SOME_NOT.getCode());
        //筛选：指定不可用,填充到返回数据中
        dealNotApplicableCoupons(returnCoupons, notApplicableCoupons, goods, sku);
    }


    /**
     * 筛选：指定可用,填充到返回数据中
     *
     * @param returnCoupons
     * @param applicableCoupons
     * @param goods
     * @param sku
     */
    private void dealApplicableCoupons(List<Coupon> returnCoupons, List<Coupon> applicableCoupons, Goods goods, GoodsSku sku) {
        if (CollectionUtils.isEmpty(applicableCoupons)) {
            return;
        }
        //筛选：指定商品条件可用
        List<Long> applicableSkuConditionCouponIds = applicableCoupons.stream().map(item -> item.getFcouponId()).collect(Collectors.toList());
        List<CouponApplicableSkuCondition> skuConditionCoupons = getApplicableSkuConditionByCouponIds(applicableSkuConditionCouponIds);

        if (CollectionUtils.isNotEmpty(skuConditionCoupons)) {
            Map<Long, List<CouponApplicableSkuCondition>> couponIdCondtionMap = skuConditionCoupons.stream().collect(Collectors.groupingBy((CouponApplicableSkuCondition::getFcouponId)));
            for (Map.Entry<Long, List<CouponApplicableSkuCondition>> parentEntry : couponIdCondtionMap.entrySet()) {
                Long couponId = parentEntry.getKey();
                List<CouponApplicableSkuCondition> conditionList = parentEntry.getValue();
                //sku条件查询里面已经用到了这个couponId，那么skuId查询里面就要把这个去掉了；
                applicableSkuConditionCouponIds.removeIf(item -> item.equals(couponId));
                //正向的话，只要有一组条件符合就符合
                for (CouponApplicableSkuCondition condition : conditionList) {
                    //这一组条件中符合的数量
                    int canReturnNum = dealCanReturnNum(condition, goods, sku);
                    if (canReturnNum == SKU_CONDITION_NUM) {
                        //只要该优惠券配置中的多组条件满足一组，即将符合条件的加入到返回list中，跳出该配置的其他组的判断，进行其他优惠券配置的判断
                        returnCoupons.add(applicableCoupons.stream().filter(item -> item.getFcouponId().equals(couponId)).findAny().get());
                        //将已经符合条件的移出需要继续判断的list
                        applicableCoupons.removeIf(item -> item.getFcouponId().equals(couponId));
                        break;
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(applicableSkuConditionCouponIds)) {
            //筛选：指定商品可用
            List<CouponApplicableSku> skuIdCoupons = getApplicableSkuByCouponIdsAndSkuId(applicableSkuConditionCouponIds, sku.getFskuId());

            if (CollectionUtils.isNotEmpty(skuIdCoupons)) {
                //指定商品可用的优惠券配置中，符合条件的优惠券配置id
                List<Long> skuCouponIds = skuIdCoupons.stream().map(item -> item.getFcouponId()).collect(Collectors.toList());
                List<Coupon> skuCoupons = applicableCoupons.stream().filter(item -> skuCouponIds.contains(item.getFcouponId())).collect(Collectors.toList());
                //将符合条件的加入到返回list中
                returnCoupons.addAll(skuCoupons);
            }
        }
    }

    /**
     * 该组条件中符合的数量
     *
     * @param condition
     * @param goods
     * @param sku
     * @return
     */
    private int dealCanReturnNum(CouponApplicableSkuCondition condition, Goods goods, GoodsSku sku) {
        int canReturnNum = 0;
        try {
            if (StringUtils.isEmpty(condition.getFcategoryId())) {
                canReturnNum++;
            } else {
                Map<String, Object> categoryMap = JacksonUtils.jsonTomap(condition.getFcategoryId());
                for (Map.Entry<String, Object> entry : categoryMap.entrySet()) {
                    String cateLevel = entry.getKey();
                    List<Long> categoryIds = (List<Long>) entry.getValue();
                    if (CATE_ONE.equals(cateLevel)) {
                        if (categoryIds.contains(sku.getFcategoryId1().intValue())) {
                            canReturnNum++;
                            break;
                        }
                    } else if (CATE_TWO.equals(cateLevel)) {
                        if (categoryIds.contains(sku.getFcategoryId2().intValue())) {
                            canReturnNum++;
                            break;
                        }
                    } else if (CATE_THREE.equals(cateLevel)) {
                        if (categoryIds.contains(sku.getFcategoryId3().intValue())) {
                            canReturnNum++;
                            break;
                        }
                    }
                }
            }

            if (StringUtils.isEmpty(condition.getFbrandId())) {
                canReturnNum++;
            } else {
                List<Long> brandIds = JacksonUtils.jsonTolist(condition.getFbrandId(), Long.class);
                if (brandIds.contains(sku.getFbrandId())) {
                    canReturnNum++;
                }
            }

            if (StringUtils.isEmpty(condition.getFlabelId())) {
                canReturnNum++;
            } else {
                List<Long> labelIds = JacksonUtils.jsonTolist(condition.getFlabelId(), Long.class);
                if (labelIds.contains(sku.getFlabelId())) {
                    canReturnNum++;
                }
            }

            if (StringUtils.isEmpty(condition.getFtradeCode())) {
                canReturnNum++;
            } else {
                List<Long> tradeCodes = JacksonUtils.jsonTolist(condition.getFtradeCode(), Long.class);
                if (tradeCodes.contains(goods.getFtradeId())) {
                    canReturnNum++;
                }
            }
        } catch (Exception e) {
            throw new BizException(MallExceptionCode.SYSTEM_ERROR);
        }
        return canReturnNum;
    }

    /**
     * 通过CouponIds和skuId查询对应的优惠券配置的sku
     *
     * @param applicableSkuConditionCouponIds
     * @return
     */
    private List<CouponApplicableSku> getApplicableSkuByCouponIdsAndSkuId(List<Long> applicableSkuConditionCouponIds, Long skuId) {
        Criteria<CouponApplicableSku, Object> skuCriteria = Criteria.of(CouponApplicableSku.class)
                .andEqualTo(CouponApplicableSku::getFskuId, skuId)
                .andIn(CouponApplicableSku::getFcouponId, applicableSkuConditionCouponIds);
        return ResultUtils.getData(applicableSkuApi.queryByCriteria(skuCriteria));
    }

    /**
     * 通过CouponIds查询对应的优惠券配置的sku条件
     *
     * @param applicableSkuConditionCouponIds
     * @return
     */
    private List<CouponApplicableSkuCondition> getApplicableSkuConditionByCouponIds(List<Long> applicableSkuConditionCouponIds) {
        Criteria<CouponApplicableSkuCondition, Object> skuConditionCriteria = Criteria.of(CouponApplicableSkuCondition.class)
                .andIn(CouponApplicableSkuCondition::getFcouponId, applicableSkuConditionCouponIds);
        return ResultUtils.getData(applicableSkuConditionApi.queryByCriteria(skuConditionCriteria));
    }


    /**
     * 筛选：指定不可用,填充到返回数据中
     *
     * @param returnCoupons
     * @param notApplicableCoupons
     * @param goods
     * @param sku
     */
    private void dealNotApplicableCoupons(List<Coupon> returnCoupons, List<Coupon> notApplicableCoupons, Goods goods, GoodsSku sku) {
        if (CollectionUtils.isEmpty(notApplicableCoupons)) {
            return;
        }
        //筛选：指定商品条件不可用
        List<Long> notApplicableConditionCouponIds = notApplicableCoupons.stream().map(item -> item.getFcouponId()).collect(Collectors.toList());
        List<CouponApplicableSkuCondition> notConditionCoupons = getApplicableSkuConditionByCouponIds(notApplicableConditionCouponIds);

        if (CollectionUtils.isNotEmpty(notConditionCoupons)) {
            Map<Long, List<CouponApplicableSkuCondition>> couponIdCondtionMap = notConditionCoupons.stream().collect(Collectors.groupingBy((CouponApplicableSkuCondition::getFcouponId)));
            for (Map.Entry<Long, List<CouponApplicableSkuCondition>> parentEntry : couponIdCondtionMap.entrySet()) {
                Long couponId = parentEntry.getKey();
                List<CouponApplicableSkuCondition> conditionList = parentEntry.getValue();
                //sku条件查询里面已经用到了这个couponId，那么skuId查询里面就要把这个去掉了；
                notApplicableConditionCouponIds.removeIf(item -> item.equals(couponId));
                //不匹配的组次数
                int num = notMatchNum(conditionList, goods, sku);
                //反向的话，只要有一组条件全部匹配上了就不符合,全部组条件都不匹配，才return，即不匹配的组次数等于list的大小
                if (num == conditionList.size()) {
                    //只要该优惠券配置中的多组条件满足一组，即将符合条件的加入到返回list中，跳出该配置的其他组的判断，进行其他优惠券配置的判断
                    returnCoupons.add(notApplicableCoupons.stream().filter(item -> item.getFcouponId().equals(couponId)).findAny().get());
                    //将已经符合条件的移出需要继续判断的list
                    notApplicableCoupons.removeIf(item -> item.getFcouponId().equals(couponId));
                }
            }
        }

        if (CollectionUtils.isNotEmpty(notApplicableConditionCouponIds)) {
            //筛选：指定商品不可用
            List<CouponApplicableSku> notSkuIdCoupons = getApplicableSkuByCouponIdsAndSkuId(notApplicableConditionCouponIds, sku.getFskuId());

            //若查询到了，就不要返回，没有查询到才返回
            if (CollectionUtils.isEmpty(notSkuIdCoupons)) {
                List<Coupon> thisReturns = notApplicableCoupons.stream().filter(item -> notApplicableConditionCouponIds.contains(item.getFcouponId())).collect(Collectors.toList());
                returnCoupons.addAll(thisReturns);
            } else {
                //查询到的不会返回的couponIds
                List<Long> thisNotReturnIds = notSkuIdCoupons.stream().map(item -> item.getFcouponId()).collect(Collectors.toList());
                //过滤出会返回的couponIds
                List<Long> thisReturnIds = notApplicableConditionCouponIds.stream().filter(item -> !thisNotReturnIds.contains(item)).collect(Collectors.toList());
                List<Coupon> thisReturns = notApplicableCoupons.stream().filter(item -> thisReturnIds.contains(item.getFcouponId())).collect(Collectors.toList());
                returnCoupons.addAll(thisReturns);
            }
        }
    }

    /**
     * 不匹配的次数
     *
     * @param conditionList
     * @param goods
     * @param sku
     * @return
     */
    private int notMatchNum(List<CouponApplicableSkuCondition> conditionList, Goods goods, GoodsSku sku) {
        //不匹配的组次数
        int size = 0;
        //反向的话，只要有一组条件全部匹配上了就不符合,全部组条件都不匹配，才return，即不匹配的组次数等于list的大小
        for (CouponApplicableSkuCondition condition : conditionList) {
            //同一组条件中，这里每组有4个条件4个条件都符合才不返回，只要一个条件不符合就返回,即返回一次不匹配的组次数
            try {
                if (StringUtils.isNotEmpty(condition.getFbrandId())) {
                    List<Long> brandIds = JacksonUtils.jsonTolist(condition.getFbrandId(), Long.class);
                    if (!brandIds.contains(sku.getFbrandId())) {
                        size++;
                        continue;
                    }
                }
                if (StringUtils.isNotEmpty(condition.getFlabelId())) {
                    List<Long> labelIds = JacksonUtils.jsonTolist(condition.getFlabelId(), Long.class);
                    if (!labelIds.contains(sku.getFlabelId())) {
                        size++;
                        continue;
                    }
                }
                if (StringUtils.isNotEmpty(condition.getFtradeCode())) {
                    List<Long> tradeCodes = JacksonUtils.jsonTolist(condition.getFtradeCode(), Long.class);
                    if (!tradeCodes.contains(goods.getFtradeId())) {
                        size++;
                        continue;
                    }
                }
                if (StringUtils.isNotEmpty(condition.getFcategoryId())) {
                    Map<String, Object> categoryMap = JacksonUtils.jsonTomap(condition.getFcategoryId());
                    int totalCompareNum = 0;
                    for (Map.Entry<String, Object> entry : categoryMap.entrySet()) {
                        String cateLevel = entry.getKey();
                        List<Long> categoryIds = (List<Long>) entry.getValue();
                        if (CATE_ONE.equals(cateLevel)) {
                            if (!categoryIds.contains(sku.getFcategoryId1().intValue())) {
                                totalCompareNum++;
                            }
                        } else if (CATE_TWO.equals(cateLevel)) {
                            if (!categoryIds.contains(sku.getFcategoryId2().intValue())) {
                                totalCompareNum++;
                            }
                        } else if (CATE_THREE.equals(cateLevel)) {
                            if (!categoryIds.contains(sku.getFcategoryId3().intValue())) {
                                totalCompareNum++;
                            }
                        }
                    }
                    //不匹配的次数等于所有组类条件的的大小
                    if (categoryMap.size() == totalCompareNum) {
                        size++;
                    }
                }
            } catch (Exception e) {
                throw new BizException(MallExceptionCode.SYSTEM_ERROR);
            }
        }
        return size;
    }


}
