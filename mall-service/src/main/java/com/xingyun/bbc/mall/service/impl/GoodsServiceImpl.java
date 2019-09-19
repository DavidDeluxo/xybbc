package com.xingyun.bbc.mall.service.impl;


import com.xingyun.bbc.common.elasticsearch.config.EsCriteria;
import com.xingyun.bbc.common.elasticsearch.config.EsManager;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.PageConfigApi;
import com.xingyun.bbc.core.operate.po.PageConfig;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.GoodsBrandApi;
import com.xingyun.bbc.core.sku.api.GoodsCategoryApi;
import com.xingyun.bbc.core.sku.api.GoodsSearchHistoryApi;
import com.xingyun.bbc.core.sku.api.GoodsSkuApi;
import com.xingyun.bbc.core.sku.po.GoodsBrand;
import com.xingyun.bbc.core.sku.po.GoodsCategory;
import com.xingyun.bbc.core.sku.po.GoodsSearchHistory;
import com.xingyun.bbc.core.sku.po.GoodsSku;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.enums.MallResultStatus;
import com.xingyun.bbc.mall.model.dto.SearchItemDto;
import com.xingyun.bbc.mall.model.vo.*;
import com.xingyun.bbc.mall.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
@Service
public class GoodsServiceImpl implements GoodsService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final String PRICE_TYPE_PREFIX = "price_type_";
    private static final String PRICE_TYPE_SUFFIX = ".min_price";

    private static final Pattern ID_PATTERN = Pattern.compile(".*Id");
    private static final Pattern NAME_PATTERN = Pattern.compile(".*Name");
    private static final Pattern SUB_PAIR_LIST_PATTERN = Pattern.compile(".*SubPairList");

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

    @Override
    public Result<SearchFilterVo> searchSkuFilter(SearchItemDto searchItemDto){

        SearchFilterVo filterVo = new SearchFilterVo();
        EsCriteria criteria = EsCriteria.build(searchItemDto);
        this.setPriceFilterCondition(searchItemDto, criteria);
        this.setAggregation(criteria);
        Map<String, Object> resultMap  = esManager.queryWithAggregation(criteria, true);


        if(resultMap.get("aggregationMap") != null){
            Map<String, Object> aggregationMap = (Map<String, Object>) resultMap.get("aggregationMap");
            //品牌
            List<Map<String, Object>> barndAggs = (List<Map<String, Object>>) aggregationMap.get("fbrand_id");
            List<BrandFilterVo> brandFilterVoList  = getNameIdPairs(BrandFilterVo.class, barndAggs, null);
            filterVo.setBrandList(brandFilterVoList);

            //原产地
            List<Map<String, Object>> originAggs = (List<Map<String, Object>>) aggregationMap.get("forigin_id");
            List<OriginFilterVo> originFilterVoList = getNameIdPairs(OriginFilterVo.class, originAggs, null);
            filterVo.setOriginList(originFilterVoList);

            //贸易类型
            List<Map<String, Object>> tradeAggs = (List<Map<String, Object>>) aggregationMap.get("ftrade_id");
            List<TradeFilterVo> tradeFilterVoList = getNameIdPairs(TradeFilterVo.class, tradeAggs, null);
            filterVo.setTradeList(tradeFilterVoList);

            //商品分类
            List<Map<String, Object>> categoryAggs = (List<Map<String, Object>>) aggregationMap.get("fcategory_id3");
            List<CategoryFilterVo> categoryFilterList = getNameIdPairs(CategoryFilterVo.class, categoryAggs, null);

            //商品属性
            List<Map<String, Object>> attributeAggs = (List<Map<String, Object>>) aggregationMap.get("attribute");
            List<GoodsAttributeFilterVo> attributeFilterList = this.getGoodAttributeList(attributeAggs);
            filterVo.setAttributeFilterList(attributeFilterList);

            Result<List<GoodsCategory>> categoryResult = goodsCategoryApi.queryByCriteria(Criteria.of(GoodsCategory.class));
            if(!categoryResult.isSuccess()){
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            if(CollectionUtils.isNotEmpty(categoryResult.getData())){
                Map<Long, List<GoodsCategory>> categoryMap = categoryResult.getData().stream().collect(Collectors.groupingBy(GoodsCategory::getFcategoryId, Collectors.toList()));
                for(CategoryFilterVo categoryFilterVo : categoryFilterList){
                    Integer fcategoryId = categoryFilterVo.getFcategoryId();
                    List<GoodsCategory> categoryList = categoryMap.get(Long.parseLong(String.valueOf(fcategoryId)));
                    if(CollectionUtils.isNotEmpty(categoryList)){
                        GoodsCategory category = categoryList.get(0);
                        categoryFilterVo.setFcategorySort(category.getFcategorySort());
                        categoryFilterVo.setFcreateTime(category.getFcreateTime());
                    }
                }
//                Collections.sort(categoryFilterList);
            }
            filterVo.setCategoryList(categoryFilterList);
            Map<String, Object> baseInfoMap = (Map<String, Object>) resultMap.get("baseInfoMap");
            if(MapUtils.isNotEmpty(baseInfoMap) && baseInfoMap.get("totalHits") != null){
                filterVo.setTotalCount(Integer.parseInt(String.valueOf(baseInfoMap.get("totalHits"))));
            }
        }
        return Result.success(filterVo);
    }

    private List<GoodsAttributeFilterVo> getGoodAttributeList(List<Map<String, Object>> attributeAggs){
        List<GoodsAttributeFilterVo> resultList = new LinkedList<>();
        if(CollectionUtils.isEmpty(attributeAggs)){
            return resultList;
        }
        List<Map<String, Object>> aggList = (List<Map<String, Object>>) attributeAggs.get(0).get("attribute_id");
        resultList = getNameIdPairs(GoodsAttributeFilterVo.class, aggList, GoodsAttributeItemFilterVo.class);

        return resultList;
    }


    @Async
    @Override
    public Result<Integer> insertSearchRecordAsync(String keyword, Integer fuid){
        log.info("插入搜索历史:{}", keyword);
        GoodsSearchHistory insertParam = new GoodsSearchHistory();
        if(fuid != null){
            insertParam.setFuid(Long.parseLong(String.valueOf(fuid)));
        }
        insertParam.setFsearchKeyword(keyword);
        Result<Integer> insertResult = goodsSearchHistoryApi.create(insertParam);
        if(!insertResult.isSuccess()){
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        return insertResult;
    }

    @Override
    public Result<List<String>> queryHotSearch(){
        List<String> resultList = new LinkedList<>();
        Result<List<PageConfig>> hotSearchResult = pageConfigApi.queryByCriteria(Criteria.of(PageConfig.class)
                .andEqualTo(PageConfig::getFisDelete,0)
                .andEqualTo(PageConfig::getFtype,5).sortDesc(PageConfig::getFcreateTime));

        if(!hotSearchResult.isSuccess()){
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if(CollectionUtils.isEmpty(hotSearchResult.getData())){
            return Result.success(resultList);
        }

        List<PageConfig> hotSearchList = hotSearchResult.getData();
        resultList = hotSearchList.stream().map(PageConfig::getFconfigName).collect(Collectors.toList());

        return Result.success(resultList);
    }


    @Override
    public Result<BrandPageVo> searchSkuBrandPage(Integer fbrandId){
        BrandPageVo brandPageVo = new BrandPageVo();
        Result<GoodsBrand> brandResult = goodsBrandApi.queryOneByCriteria(Criteria.of(GoodsBrand.class)
                .andEqualTo(GoodsBrand::getFbrandId, fbrandId));
        if(!brandResult.isSuccess()){
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        if(brandResult.getData() != null){
            GoodsBrand goodsBrand  = brandResult.getData();
            if(goodsBrand.getFisDelete() == 1){
                throw new BizException(MallResultStatus.BRAND_IS_DELETED);
            }
            brandPageVo.setFbrandLogo(goodsBrand.getFbrandLogo());
            brandPageVo.setFbrandName(goodsBrand.getFbrandName());
            brandPageVo.setFbrandDesc(goodsBrand.getFbrandDesc());
            brandPageVo.setFbrandId(goodsBrand.getFbrandId());
            brandPageVo.setFbrandPoster(goodsBrand.getFbrandPoster());
            brandPageVo.setForiginName(goodsBrand.getFcountryName());
            // 品牌商品总数
            Result<Integer> goodsCountResult = goodsSkuApi.countByCriteria(Criteria.of(GoodsSku.class).andEqualTo(GoodsSku::getFisDelete, 0));
            if(!goodsCountResult.isSuccess()){
                throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            }
            brandPageVo.setFgoodsTotalCount(goodsCountResult.getData());
        }
        return Result.success(brandPageVo);
    }





    private <T,U> List<T> getNameIdPairs(Class<T> clazz, List<Map<String, Object>> aggregationList, Class<U> clazz2){
        Field id_field = null;
        Field name_field = null;
        Field sub_pair_list_field = null;
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields){
           String fieldName = field.getName();
           if(id_field == null && ID_PATTERN.matcher(fieldName).matches()){
               id_field = field;
           }
           if(name_field == null && NAME_PATTERN.matcher(fieldName).matches()){
               name_field = field;
           }
           if(sub_pair_list_field == null && SUB_PAIR_LIST_PATTERN.matcher(fieldName).matches()){
               ParameterizedType pType = (ParameterizedType) field.getGenericType();
               Type actualType = pType.getActualTypeArguments()[0];
                if(actualType.getTypeName().equals(clazz2.getTypeName())){
                    sub_pair_list_field = field;
                }
           }
        }

        if(id_field == null){
            throw new RuntimeException("没有找到id属性");
        }
        if(name_field == null){
            throw new RuntimeException("没有找到名称属性");
        }
        id_field.setAccessible(true);
        name_field.setAccessible(true);
        List<T> resultList = new LinkedList<>();
        if(CollectionUtils.isEmpty(aggregationList)){
            return resultList;
        }
        try {
            for (Map<String, Object> idMap : aggregationList){
                Object valueObject = clazz.newInstance();
                Object idValue = idMap.get(EsManager.AGGREGATION_KEY_NAME);
                if(idMap != null){
                    // set Id
                    if(!id_field.getType().equals(Integer.class)){
                        throw new IllegalArgumentException();
                    }
                    id_field.set(valueObject, Integer.parseInt(String.valueOf(idValue)));
                    Map<String, Object> nameMap  = (Map<String, Object>) idMap.get(EsManager.SUBAGGREGATION_NAME);
                    if(MapUtils.isNotEmpty(nameMap)){
                        Object[] nameMapValues = nameMap.values().toArray();
                        List<Map<String, Object>> nameList = (List<Map<String, Object>>) nameMapValues[0];
                        if(CollectionUtils.isNotEmpty(nameList)){
                            Object nameValue = nameList.get(0).get(EsManager.AGGREGATION_KEY_NAME);
                            if(!name_field.getType().equals(String.class)){
                                throw new IllegalArgumentException();
                            }
                            name_field.set(valueObject, String.valueOf(nameValue));

                            Map<String, Object> subAggMap = (Map<String, Object>) nameList.get(0).get(EsManager.SUBAGGREGATION_NAME);
                            if(MapUtils.isNotEmpty(subAggMap) && clazz2 != null && sub_pair_list_field != null){
                                Object[] subAggArray = subAggMap.values().toArray();
                                List<Map<String, Object>> subAggList = (List<Map<String, Object>>) subAggArray[0];
                                List<U> subPairList = this.getNameIdPairs(clazz2, subAggList, null);
                                sub_pair_list_field.setAccessible(true);
                                sub_pair_list_field.set(valueObject, subPairList);
                            }
                        }
                    }
                }
                resultList.add( (T) valueObject);
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("内部调用错误");
        }
        return resultList;
    }

    private void setPriceFilterCondition(SearchItemDto searchItemDto, EsCriteria criteria){
        if(criteria == null){
            return;
        }
        String priceFieldName = PRICE_TYPE_PREFIX + searchItemDto.getFuserTypeId() + PRICE_TYPE_SUFFIX;

        if(searchItemDto.getPriceOrderBy() != null){
            criteria.sortBy(priceFieldName, searchItemDto.getPriceOrderBy());
        }

        if(searchItemDto.getFpriceStart() != null){
            BigDecimal startPrice_yuan = searchItemDto.getFpriceStart();
            BigDecimal startPrice_penny = startPrice_yuan.multiply(ONE_HUNDRED).setScale(0, BigDecimal.ROUND_HALF_UP);
            criteria.rangeFrom(priceFieldName, String.valueOf(startPrice_penny));
        }

        if(searchItemDto.getFpriceEnd() != null){
            BigDecimal endPrice_yuan = searchItemDto.getFpriceEnd();
            BigDecimal endPrice_penny = endPrice_yuan.multiply(ONE_HUNDRED).setScale(0, BigDecimal.ROUND_HALF_UP);
            criteria.rangeTo(priceFieldName, String.valueOf(endPrice_penny));
        }
    }


    @Override
    public Result<PageVo<SearchItemVo>> searchSkuList(SearchItemDto searchItemDto){
        if(!StringUtils.isEmpty(searchItemDto.getSearchFullText())){
            this.insertSearchRecordAsync(searchItemDto.getSearchFullText(), searchItemDto.getFuid());
        }

        PageVo<SearchItemVo> pageVo = new PageVo<>();
        pageVo.setTotalCount(0);
        pageVo.setPageSize(1);
        EsCriteria criteria = EsCriteria.build(searchItemDto);
        if(CollectionUtils.isNotEmpty(searchItemDto.getFattributeItemId())){
            String fieldname = "attributes.fclass_attribute_item_id";
            DisMaxQueryBuilder disMaxQuerys = QueryBuilders.disMaxQuery();
            for(Object value : searchItemDto.getFattributeItemId()){
                disMaxQuerys.add(QueryBuilders.termsQuery(fieldname, value));
            }
            criteria.getFilterBuilder().must(QueryBuilders.nestedQuery("attributes", disMaxQuerys, ScoreMode.None));
        }

        this.setPriceFilterCondition(searchItemDto, criteria);
        if(searchItemDto.getIsStockNotEmpty() != null && searchItemDto.getIsStockNotEmpty() == 1){
            criteria.rangeFrom("fstock_remain_num_total", 1);
        }
        this.addSoldOutCondition(criteria);

        String dateScript = "DateFormat.getInstance().parse(doc['@timestamp'])";
        String soldAmountScript = "DateFormat.getInstance().parse(doc['@timestamp']) + 1-Math.pow(doc['fsell_total'].value + 1, -1)";
        Map<String, Object> resultMap = esManager.functionQueryForResponse(criteria, soldAmountScript, CombineFunction.SUM);
        List<Map<String, Object>> resultList = (List<Map<String, Object>>) resultMap.get("resultList");

        List<SearchItemVo> voList = new LinkedList<>();
        pageVo.setList(voList);
        Map<String, Object> baseInfoMap = (Map<String, Object>) resultMap.get("baseInfoMap");
        if(!CollectionUtils.isEmpty(resultList)){
            for (Map<String, Object> map : resultList){
                SearchItemVo vo = new SearchItemVo();
                if(map.get("fskuId") != null){
                    vo.setFskuId(Integer.parseInt(String.valueOf(map.get("fskuId"))));
                }
                if(map.get("fskuName") != null){
                    vo.setFskuName(String.valueOf(map.get("fskuName")));
                }
                if(map.get("ftradeId") != null){
                    vo.setFtradedId(Integer.parseInt(String.valueOf(map.get("ftradeId"))));
                }
                if(map.get("ftradeName") != null){
                    vo.setFtradeName(String.valueOf(map.get("ftradeName")));
                }
                if(map.get("fsellTotal") != null){
                    vo.setFsellNum(Long.parseLong(String.valueOf(map.get("fsellTotal"))));
                }
                if(map.get("fskuThumbImage") != null){
                    vo.setFimgUrl(String.valueOf(map.get("fskuThumbImage")));
                }
                if(map.get("fgoodsId") != null){
                    vo.setFgoodsId(Integer.parseInt(String.valueOf(map.get("fgoodsId"))));
                }
                if(map.get("fskuStatus") != null){
                    vo.setFskuStatus(Integer.parseInt(String.valueOf(map.get("fskuStatus"))));
                }
                if(map.get("flabelId") != null){
                    vo.setFlabelId(Integer.parseInt(String.valueOf(map.get("flabelId"))));
                }
                if(map.get("prices") != null){
                    List<Map<String, Object>> priceList = (List<Map<String, Object>>) map.get("prices");
                    if(CollectionUtils.isNotEmpty(priceList)){
                      Map<String, List<Map<String, Object>>> priceMap  = priceList.stream()
                              .collect(Collectors.groupingBy(o-> String.valueOf(o.get("fuser_type_id")), Collectors.toList()));
                      if(priceMap.get(searchItemDto.getFuserTypeId()) != null){
                         List<Map<String, Object>> priceListofType = priceMap.get(searchItemDto.getFuserTypeId());
                          if(CollectionUtils.isNotEmpty(priceListofType)){
                             Map<String, Object> price = priceListofType.get(0);
                             if(price.get("min_price") != null){
                                 BigDecimal price_penny = new BigDecimal(String.valueOf(price.get("min_price")));
                                 BigDecimal price_yuan = price_penny.divide(ONE_HUNDRED).setScale(2, BigDecimal.ROUND_HALF_UP);
                                 vo.setFbatchSellPrice(price_yuan);
                             }else {
                                 vo.setFbatchSellPrice(BigDecimal.ZERO);
                             }
                          }
                      }
                    }
                }
                if(map.get("fstockRemainNumTotal") != null){
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

    private void addSoldOutCondition(EsCriteria criteria){
        if(criteria == null){
            return;
        }
        DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
        RangeQueryBuilder onOut = QueryBuilders.rangeQuery("fstock_remain_num_total").gt(0);
        TermQueryBuilder soldOut = QueryBuilders.termQuery("fstock_remain_num_total", 0).boost(Integer.MIN_VALUE);
        disMaxQueryBuilder.add(onOut).add(soldOut);
        criteria.getFilterBuilder().must(disMaxQueryBuilder);
    }

    /**
     * 添加聚合条件
     * @param criteria
     */
    private void setAggregation(EsCriteria criteria){
        if(criteria == null){
            return;
        }
        criteria.termAggregate("fcategory_id3", "fcategory_id3").subAggregate("fcategory_name3", "fcategory_name3.keyword");
        criteria.termAggregate("fbrand_id","fbrand_id").subAggregate("fbrand_name", "fbrand_name.keyword");
        criteria.termAggregate("forigin_id", "forigin_id").subAggregate("forigin_name","forigin_name.keyword");
        criteria.termAggregate("ftrade_id", "ftrade_id").subAggregate("ftrade_name","ftrade_name.keyword");

        AggregationBuilder attributeAgg = AggregationBuilders.terms("attribute_id").field("attributes.fclass_attribute_id").order(BucketOrder.count(false))
                .subAggregation(AggregationBuilders.terms("attribute_name").field("attributes.fclass_attribute_name.keyword").order(BucketOrder.count(false))
                .subAggregation(AggregationBuilders.terms("attribute_item_id").field("attributes.fclass_attribute_item_id").order(BucketOrder.count(false))
                .subAggregation(AggregationBuilders.terms("attribute_item_value").field("attributes.fclass_attribute_item_val.keyword").order(BucketOrder.count(false)))));

        AggregationBuilder nestedAgg = AggregationBuilders.nested("attribute", "attributes").subAggregation(attributeAgg);
        criteria.getAggBuilders().put("attribute", nestedAgg);
    }


}
