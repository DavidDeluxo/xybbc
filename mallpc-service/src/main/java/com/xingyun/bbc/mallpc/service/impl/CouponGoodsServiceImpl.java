package com.xingyun.bbc.mallpc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.market.api.CouponApi;
import com.xingyun.bbc.core.market.api.CouponApplicableSkuApi;
import com.xingyun.bbc.core.market.api.CouponApplicableSkuConditionApi;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.market.po.CouponApplicableSku;
import com.xingyun.bbc.core.market.po.CouponApplicableSkuCondition;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.GoodsCouponApi;
import com.xingyun.bbc.core.sku.dto.ItemDto;
import com.xingyun.bbc.core.sku.vo.ItemVo;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.core.utils.StringUtil;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.PriceUtil;
import com.xingyun.bbc.mallpc.model.dto.search.SearchItemDto;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemListVo;
import com.xingyun.bbc.mallpc.model.vo.search.SearchItemVo;
import com.xingyun.bbc.mallpc.service.CouponGoodsService;
import com.xingyun.bbc.mallpc.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hekaijin
 * @date 2019/11/20 11:36
 * @Description
 */
@Slf4j
@Service
public class CouponGoodsServiceImpl implements CouponGoodsService {

    @Resource
    private GoodsService goodsService;
    @Resource
    private GoodsCouponApi couponGoodsApi;
    @Resource
    private UserApi userApi;
    @Resource
    private CouponApi couponApi;
    @Resource
    private CouponApplicableSkuApi couponApplicableSkuApi;
    @Resource
    private CouponApplicableSkuConditionApi couponApplicableSkuConditionApi;

    @Override
    public Result<SearchItemListVo<SearchItemVo>> queryGoodsList(SearchItemDto dto) {

        Result<SearchItemListVo<SearchItemVo>> res = new Result<>();
        SearchItemListVo<SearchItemVo> pageVo = new SearchItemListVo<>();
        pageVo.setIsLogin(dto.getIsLogin());
        pageVo.setTotalCount(0);
        pageVo.setCurrentPage(dto.getPageIndex());
        pageVo.setPageSize(dto.getPageSize());
        pageVo.setHasNext(false);
        pageVo.setHasPrevious(false);
        pageVo.setPageCount(0);
        pageVo.setList(Lists.newArrayList());
        res.setData(pageVo);

        try {
            res = goodsService.searchSkuList(dto);
            if (!res.isSuccess())
                throw new Exception();

        } catch (Exception e) {
            log.warn("ES优惠券商品搜索失败!...");

//            if (Objects.nonNull(dto.getFcouponId())){
//
//                log.info("----------ES优惠券商品搜索失败!,转SQL查询------------");
//                res = this.queryGoodsListRealTime(dto);
//            }

        }
        return res;

    }

    @Override
    public Result<SearchItemListVo<SearchItemVo>> queryGoodsListRealTime(SearchItemDto dto) {
        CouponSkuCondition couponSkuCondition = new CouponSkuCondition(dto).invoke();

        if (couponSkuCondition.is()) return Result.success(this.getInitListVo(dto));

        List<CouponApplicableSkuCondition> skuConResData = couponSkuCondition.getSkuConResData();

        if (!CollectionUtils.isEmpty(skuConResData)) this.buildCategory(dto, skuConResData);

        ItemDto itemDto = new ItemDto();
        BeanUtils.copyProperties(dto, itemDto);
        itemDto.setPageNum(dto.getPageIndex());
        if (dto.getIsLogin()) {
            Integer priceType = this.getUserPriceType(dto);
            itemDto.setFuserTypeId(priceType + "");
        }
        Result<Page<ItemVo>> pageResult = couponGoodsApi.queryCouponGoods(itemDto);

        if (!pageResult.isSuccess()) throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);

        Page<ItemVo> data = pageResult.getData();
        if (CollectionUtils.isEmpty(data.getResult())) return Result.success(this.getInitListVo(dto));

        List<SearchItemVo> list = new ArrayList<SearchItemVo>(data.getResult().size());
        data.getResult().forEach(da -> {
            SearchItemVo vo = new SearchItemVo();
            BeanUtils.copyProperties(da, vo);
            if (dto.getIsLogin() && Objects.nonNull(vo.getFbatchSellPrice())) {
                vo.setFbatchSellPrice(PriceUtil.toYuan(vo.getFbatchSellPrice()));
            } else {
                vo.setFbatchSellPrice(BigDecimal.ZERO);
            }
            list.add(vo);
        });

        SearchItemListVo<SearchItemVo> itemListVo = new SearchItemListVo<SearchItemVo>();
        itemListVo.setIsLogin(dto.getIsLogin());
        itemListVo.setCurrentPage(dto.getPageIndex());
        itemListVo.setPageSize(dto.getPageSize());
        itemListVo.setList(list);
        itemListVo.setTotalCount(list.size());
        return Result.success(itemListVo);
    }

    /**
     * 根据用户身份选择价格类型
     *
     * @param dto
     * @return
     */
    private Integer getUserPriceType(SearchItemDto dto) {
        //默认为未认证
        Integer userTypeId = 0;
        if (dto.getIsLogin() && Objects.nonNull(dto.getFuid())) {
            Result<User> userResult = userApi.queryOneByCriteria(Criteria.of(User.class).andEqualTo(User::getFuid, dto.getFuid()));

            if (!userResult.isSuccess()) throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            User user = userResult.getData();
            if (Objects.isNull(user)) throw new BizException(MallPcExceptionCode.USER_NOT_EXIST);

            userTypeId = user.getFoperateType();
        }
        return userTypeId;
    }

    private class CouponSkuCondition {
        private boolean myResult;
        private SearchItemDto dto;
        private List<CouponApplicableSkuCondition> skuConResData;

        public CouponSkuCondition(SearchItemDto dto) {
            this.dto = dto;
        }

        boolean is() {
            return myResult;
        }

        public List<CouponApplicableSkuCondition> getSkuConResData() {
            return skuConResData;
        }

        public CouponSkuCondition invoke() {
            Result<Coupon> couponResult = couponApi.queryById(dto.getFcouponId());
            Ensure.that(couponResult.isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);

            if (Objects.isNull(couponResult.getData())) {
                myResult = true;
                return this;
            }

            Result<List<CouponApplicableSku>> skuListRes = couponApplicableSkuApi.queryByCriteria(Criteria.of(CouponApplicableSku.class).andEqualTo(CouponApplicableSku::getFcouponId, dto.getFcouponId()));
            Ensure.that(skuListRes.isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);

            //skuId
            if (!CollectionUtils.isEmpty(skuListRes.getData())) {
                dto.setFskuIds(skuListRes.getData().stream().map(CouponApplicableSku::getFskuId).collect(Collectors.toList()));
            }

            Result<List<CouponApplicableSkuCondition>> skuConRes = couponApplicableSkuConditionApi.queryByCriteria(Criteria.of(CouponApplicableSkuCondition.class).andEqualTo(CouponApplicableSkuCondition::getFcouponId, dto.getFcouponId()));
            Ensure.that(skuConRes.isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);

            skuConResData = skuConRes.getData();
            myResult = false;
            return this;
        }
    }

    public SearchItemListVo<SearchItemVo> getInitListVo(SearchItemDto dto) {
        return new SearchItemListVo<>(0, dto.getPageIndex(), dto.getPageSize(), Lists.newArrayList());
    }

    private void buildCategory(SearchItemDto dto, List<CouponApplicableSkuCondition> skuConResData) {
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
            if (StringUtil.isNotBlank(categoryIdJson) && categoryIdJson.contains("[")) {
                this.addCategoryIds(categoryIdL1, categoryIdL2, categoryIdL3, categoryIdJson);
            }

            this.addTypes(brandId, labelId, tradeId, sc);

        });

        if (categoryIdL1.size() > 0) dto.setFcategoryIdL1(toList(categoryIdL1));

        if (categoryIdL2.size() > 0) dto.setFcategoryIdL2(toList(categoryIdL2));

        if (categoryIdL3.size() > 0) dto.setFcategoryId(toList(categoryIdL3));

        if (brandId.size() > 0) dto.setFbrandId(toList(brandId));

        if (labelId.size() > 0) dto.setFlabelId(toList(labelId));

        if (tradeId.size() > 0) dto.setFtradeId(toList(tradeId));
    }

    private List<Integer> toList(Set<Integer> set) {
        if (CollectionUtils.isEmpty(set)) return Lists.newArrayList();

        List<Integer> list = new ArrayList<Integer>(set.size());
        set.forEach(s -> list.add(s));

        return list;
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

    private void addTypes(Set<Integer> brandId, Set<Integer> labelId, Set<Integer> tradeId, CouponApplicableSkuCondition sc) {
        String fbrandId = sc.getFbrandId();
        String flabelId = sc.getFlabelId();
        String ftradeCode = sc.getFtradeCode();

        if (StringUtil.isNotBlank(fbrandId) && fbrandId.contains("[")) {
            JSONArray brands = JSON.parseArray(fbrandId);
            brands.forEach(b -> brandId.add((Integer) b));
        }
        if (StringUtil.isNotBlank(flabelId) && flabelId.contains("[")) {
            JSONArray labelIds = JSON.parseArray(flabelId);
            labelIds.forEach(b -> labelId.add((Integer) b));
        }
        if (StringUtil.isNotBlank(ftradeCode) && ftradeCode.contains("[")) {
            JSONArray tradeCodes = JSON.parseArray(ftradeCode);
            tradeCodes.forEach(b -> tradeId.add((Integer) b));
        }
    }
}
