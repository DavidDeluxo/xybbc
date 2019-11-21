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
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.FileUtils;
import com.xingyun.bbc.mallpc.model.dto.search.SearchItemDto;
import com.xingyun.bbc.mallpc.model.vo.search.*;
import com.xingyun.bbc.mallpc.service.GoodsService;
import com.xingyun.bbc.mallpc.service.SearchRecordService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final String PRICE_TYPE_PREFIX_CAMEL = "priceType";
    private static final String PRICE_TYPE_SUFFIX = ".min_price";
    private static Pattern humpPattern = Pattern.compile("[A-Z0-9]");

    @Autowired
    private EsManager esManager;
    @Resource
    private UserApi userApi;
    @Resource
    private GoodsCategoryApi goodsCategoryApi;
    @Autowired
    private SearchRecordService searchRecordService;

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
//                    vo.setFimgUrl(FileUtils.getFileUrl(String.valueOf(map.get("fskuThumbImage"))));
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
     * 分类查询条件设置
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
                throw new BizException(MallPcExceptionCode.USER_NOT_EXIST);
            }
            User user = userResult.getData();
            fuserTypeId = String.valueOf(user.getFoperateType());
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
}
