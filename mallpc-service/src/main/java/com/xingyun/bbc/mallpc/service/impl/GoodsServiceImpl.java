package com.xingyun.bbc.mallpc.service.impl;

import com.xingyun.bbc.common.elasticsearch.config.EsBeanUtil;
import com.xingyun.bbc.common.elasticsearch.config.EsCriteria;
import com.xingyun.bbc.common.elasticsearch.config.EsManager;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.GoodsCategoryApi;
import com.xingyun.bbc.core.sku.po.GoodsCategory;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.enums.UserVerifyStatusEnum;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.components.RedisHolder;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.RandomUtils;
import com.xingyun.bbc.mallpc.common.utils.ResultUtils;
import com.xingyun.bbc.mallpc.model.dto.search.SearchItemDto;
import com.xingyun.bbc.mallpc.model.vo.TokenInfoVo;
import com.xingyun.bbc.mallpc.model.vo.index.CateSearchItemListVo;
import com.xingyun.bbc.mallpc.model.vo.search.*;
import com.xingyun.bbc.mallpc.service.GoodsService;
import com.xingyun.bbc.mallpc.service.SearchRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant.PC_MALL_CATE_SKU;

@Slf4j
@Service
public class GoodsServiceImpl implements GoodsService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final String PRICE_TYPE_PREFIX_CAMEL = "priceType";
    private static final String PRICE_TYPE_SUFFIX = ".min_price";
    private static Pattern humpPattern = Pattern.compile("[A-Z0-9]");
    private static final String COUPON_ALIAS_PREFIX = "coupon_";

    @Autowired
    private EsManager esManager;
    @Resource
    private UserApi userApi;
    @Resource
    private GoodsCategoryApi goodsCategoryApi;
    @Autowired
    private SearchRecordService searchRecordService;
    @Autowired
    private RedisHolder redisHolder;
    /**
     * 查询商品列表
     *
     * @param searchItemDto
     * @return
     */
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
        if (searchItemDto.getFcouponId() != null) {
            criteria.setIndexName(this.getCouponAliasName(Long.parseLong(String.valueOf(searchItemDto.getFcouponId()))));
        }
        this.setSearchCondition(searchItemDto, criteria);
        String soldAmountScript = "1-Math.pow(doc['fsell_total'].value + 1, -1)";
        Map<String, Object> resultMap = esManager.functionQueryForResponse(criteria, soldAmountScript, CombineFunction.SUM);
        List<Map<String, Object>> resultList = (List<Map<String, Object>>) resultMap.get("resultList");
        List<SearchItemVo> voList = new LinkedList<>();
        pageVo.setList(voList);
        Map<String, Object> baseInfoMap = (Map<String, Object>) resultMap.get("baseInfoMap");
        if (!CollectionUtils.isEmpty(resultList)) {
            String priceName = searchItemDto.getPriceName();
            if (StringUtils.isEmpty(priceName)) {
                priceName = this.getUserPriceType(searchItemDto.getIsLogin(), searchItemDto.getFuid(), searchItemDto.getFoperateType(), searchItemDto.getFverifyStatus());
            }
            for (Map<String, Object> map : resultList) {
                SearchItemVo vo = getSearchItemVo(map, priceName, searchItemDto.getIsLogin());
                voList.add(vo);
            }
            pageVo.setPageSize(searchItemDto.getPageSize());
            pageVo.setCurrentPage(searchItemDto.getPageIndex());
            pageVo.setTotalCount(Integer.parseInt(String.valueOf(baseInfoMap.get("totalHits"))));
        }

        return Result.success(pageVo);
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

    /**
     * 查询商品筛选信息列表
     *
     * @param searchItemDto
     * @return
     */
    @Override
    public Result<SearchFilterVo> searchSkuFilter(SearchItemDto searchItemDto) {
        SearchFilterVo filterVo = new SearchFilterVo();
        //分类查询条件设置
        this.setCategoryCondition(searchItemDto);
        //扫描入参类属性注解, 自动构建搜索条件
        EsCriteria criteria = EsCriteria.build(searchItemDto);
        if (searchItemDto.getFcouponId() != null) {
            criteria.setIndexName(this.getCouponAliasName(Long.parseLong(String.valueOf(searchItemDto.getFcouponId()))));
        }
        //设定搜索条件
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

            //商品一级分类
            List<Map<String, Object>> categoryAggs = (List<Map<String, Object>>) aggregationMap.get("fcategory_id1");
            List<CategoryFilterVo> categoryFilterList = EsBeanUtil.getValueObjectList(CategoryFilterVo.class, categoryAggs);
            if (!CollectionUtils.isEmpty(categoryFilterList)) {
                List<Integer> cateIds = categoryFilterList.stream().map(item -> item.getFcategoryId()).collect(Collectors.toList());
                Criteria<GoodsCategory, Object> criteriaCate = Criteria.of(GoodsCategory.class)
                        .andIn(GoodsCategory::getFcategoryId, cateIds);
                Result<List<GoodsCategory>> categoryResult = goodsCategoryApi.queryByCriteria(criteriaCate);
                List<GoodsCategory> cateList = ResultUtils.getData(categoryResult);
                if (CollectionUtils.isNotEmpty(cateList)) {
                    Map<Long, GoodsCategory> categoryMap = categoryResult.getData().stream().collect(Collectors.toMap(GoodsCategory::getFcategoryId, cate -> cate));
                    for (CategoryFilterVo categoryFilterVo : categoryFilterList) {
                        Integer categoryId = categoryFilterVo.getFcategoryId();
                        GoodsCategory category = categoryMap.get(Long.parseLong(String.valueOf(categoryId)));
                        if (category != null) {
                            categoryFilterVo.setFcategorySort(category.getFcategorySort());
                            categoryFilterVo.setFcreateTime(category.getFcreateTime());
                        }
                    }
                }
            }
            filterVo.setCategoryList(categoryFilterList);

            //赋值totalCount
            Map<String, Object> baseInfoMap = (Map<String, Object>) resultMap.get("baseInfoMap");
            if (MapUtils.isNotEmpty(baseInfoMap) && baseInfoMap.get("totalHits") != null) {
                filterVo.setTotalCount(Integer.parseInt(String.valueOf(baseInfoMap.get("totalHits"))));
            }
        }

        return Result.success(filterVo);
    }

//    @Override
//    public Result<List<CateSearchItemListVo>> floorSkus(List<Integer> cateIds, TokenInfoVo infoVo) {
//        if (CollectionUtils.isEmpty(cateIds)) {
//            return Result.success(new ArrayList<>());
//        }
//        List<CateSearchItemListVo> resultList = new ArrayList<>();
//        String priceName = this.getUserPriceType(infoVo.getIsLogin(), infoVo.getFuid(), infoVo.getFoperateType(), infoVo.getFverifyStatus());
//
//        List<CompletableFuture<CateSearchItemListVo>> completableFutureList = new ArrayList<>();
//
//        for (Integer cateId : cateIds) {
//            CompletableFuture<CateSearchItemListVo> result = CompletableFuture.supplyAsync(() -> getSkuList(cateId, infoVo, priceName));
//            completableFutureList.add(result);
//        }
//        CompletableFuture
//                .allOf(completableFutureList.toArray(new CompletableFuture[completableFutureList.size()]))
//                .join();
//        for (CompletableFuture completableFuture : completableFutureList) {
//            try {
//                resultList.add((CateSearchItemListVo) completableFuture.get());
//            } catch (Exception e) {
//                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//        return Result.success(resultList);
//    }

    /**
     * 封装查询条件查询
     *
     * @param cateId
     * @param infoVo
     * @param priceName
     * @return
     */
    private CateSearchItemListVo getSkuList(Integer cateId, TokenInfoVo infoVo, String priceName) {
        String key = new StringBuilder().append(PC_MALL_CATE_SKU).append(cateId).append("_").append(priceName).toString();
        List<SearchItemVo> voList;
        long start = System.currentTimeMillis();
        if (redisHolder.exists(key)) {
            voList = (List<SearchItemVo>) redisHolder.getObject(key);
            log("缓存",cateId,System.currentTimeMillis()-start);
        } else {
            SearchItemDto searchItemDto = new SearchItemDto();
            List<Integer> cateIdList = new ArrayList<>();
            cateIdList.add(cateId);
            searchItemDto.setFcategoryIdL1(cateIdList);
            searchItemDto.setIsLogin(infoVo.getIsLogin());
            searchItemDto.setFuid(infoVo.getFuid());
            searchItemDto.setSellAmountOrderBy("desc");
            searchItemDto.setPriceName(priceName);
            Result<SearchItemListVo<SearchItemVo>> result = searchSkuList(searchItemDto);
            voList = result.getData().getList();
            //缓存有效期随机数10到40秒之间
            long timeout = RandomUtils.randomLong(30) + 10;
            redisHolder.set(key,voList,timeout);
            log("搜索引擎",cateId,System.currentTimeMillis()-start);
        }

        CateSearchItemListVo vo = new CateSearchItemListVo();
        vo.setCateId(cateId);
        vo.setSkus(voList);
        return vo;
    }

    /**
     * 首页耗时日志记录
     * @param model
     * @param cateId
     * @param time
     */
    private void log(String model, Integer cateId, long time){
        if(log.isDebugEnabled()){
            String info = new StringBuilder().append("一级分类").append(cateId).append(model).append("，首页楼层商品耗时: ").append(time).toString();
            log.debug(info);
        }
    }

    /**
     * 单个skuVo的封装
     *
     * @param map
     * @param priceName
     * @param isLogin
     * @return
     */
    private SearchItemVo getSearchItemVo(Map<String, Object> map, String priceName, Boolean isLogin) {
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
        if (map.get(priceName) != null && isLogin) {
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
        return vo;
    }


    /**
     * 分类查询条件设置
     *
     * @param searchItemDto
     */
    private void setCategoryCondition(SearchItemDto searchItemDto) {
        // 商品类目条件
        if (CollectionUtils.isEmpty(searchItemDto.getFUnicategoryIds()) || searchItemDto.getFcategoryLevel() == null) {
            return;
        }
        Integer categoryLevel = searchItemDto.getFcategoryLevel();
        if (categoryLevel == 1) {
            searchItemDto.setFcategoryIdL1(searchItemDto.getFUnicategoryIds());
        } else if (categoryLevel == 2) {
            searchItemDto.setFcategoryIdL2(searchItemDto.getFUnicategoryIds());
        } else if (categoryLevel == 3) {
            searchItemDto.setFcategoryId(searchItemDto.getFUnicategoryIds());
        }
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
        // 库存条件
        this.setStockCondition(searchItemDto, criteria);
        // 价格条件
        this.setPriceCondition(searchItemDto, criteria);
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
        criteria.termAggregate("fbrand_id", "fbrand_id").subAggregate("fbrand_name", "fbrand_name.keyword");
        criteria.termAggregate("fcategory_id1", "fcategory_id1").subAggregate("fcategory_name1", "fcategory_name1.keyword");
        criteria.termAggregate("forigin_id", "forigin_id").subAggregate("forigin_name", "forigin_name.keyword");
        criteria.termAggregate("ftrade_id", "ftrade_id").subAggregate("ftrade_name", "ftrade_name.keyword");
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
     * 价格条件
     *
     * @param searchItemDto
     * @param criteria
     */
    private void setPriceCondition(SearchItemDto searchItemDto, EsCriteria criteria) {
        if (criteria == null) {
            return;
        }

        String priceFieldName = this.getUserPriceType(searchItemDto.getIsLogin(), searchItemDto.getFuid(), searchItemDto.getFoperateType(), searchItemDto.getFverifyStatus());
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
     * 根据用户身份选择价格类型
     *
     * @return
     */
    private String getUserPriceType(Boolean isLogin, Integer fuid, Integer foperateType, Integer fverifyStatus) {
        //默认为未认证
        String fuserTypeId = "0";
        if (isLogin && fuid != null) {
            //若登录信息里面存的用户认证类型和认证状态可用，不用查库
            if(Objects.equals(UserVerifyStatusEnum.AUTHENTICATED.getCode(), fverifyStatus)){
                fuserTypeId = String.valueOf(foperateType);
            }else{
                Result<User> userResult = userApi.queryOneByCriteria(Criteria.of(User.class)
                        .fields(User::getFuid, User::getFoperateType, User::getFverifyStatus)
                        .andEqualTo(User::getFuid, fuid));
                User user = ResultUtils.getDataNotNull(userResult, MallPcExceptionCode.USER_NOT_EXIST);
                if(Objects.equals(UserVerifyStatusEnum.AUTHENTICATED.getCode(), user.getFverifyStatus())){
                    fuserTypeId = String.valueOf(user.getFoperateType());
                }
            }
        }
        String priceName = PRICE_TYPE_PREFIX_CAMEL + fuserTypeId;
        return priceName;
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
}
