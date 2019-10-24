package com.xingyun.bbc.common.elasticsearch.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class EsManager {

    public static final String AGGREGATION_KEY_NAME = "key";
    public static final String SUBAGGREGATION_NAME = "subaggregation";


    @Autowired
    EsSettingsProperties properties;

    private RestHighLevelClient client;


    public EsManager(RestHighLevelClient client) {
        this.client = client;
    }


    /**
     * 通过搜索条件查询搜索引擎
     *
     * @param criteria
     * @return
     * @throws Exception
     */
    private SearchResponse queryForResponse(EsCriteria criteria, QueryBuilder builder) throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(properties.getIndex());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索条件
        searchSourceBuilder.query(builder);
        //分页条件
        searchSourceBuilder.from(criteria.getStartIndext()).size(criteria.getPageSize());
        //排序条件
        if (CollectionUtils.isNotEmpty(criteria.getSorts())) {
            List<SortBuilder> sorts = criteria.getSorts();
            for (SortBuilder sortBuilder : sorts) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }
        //自定义包含字段条件
        searchSourceBuilder.fetchSource(criteria.getIncludeFields(), criteria.getExcludeFields());
        //聚合条件
        criteria.getAggBuilders().forEach((Key, value) -> searchSourceBuilder.aggregation(value));
        //高亮条件
        searchSourceBuilder.highlighter(criteria.getHighlightBuilder());
        searchRequest.source(searchSourceBuilder);
        SearchResponse sResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return sResponse;
    }

    private SearchResponse queryForResponse(EsCriteria criteria) throws Exception {
        return this.queryForResponse(criteria, criteria.getFilterBuilder());
    }



    /**
     * 提取搜索结果列表
     *
     * @param SearchResponse sResponse
     * @return
     */
    private List<Map<String, Object>> getHitList(SearchResponse sResponse) {
        List<Map<String, Object>> resList = new LinkedList<>();
        try {
            for (SearchHit hit : sResponse.getHits().getHits()) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.putAll(convertUpperUndercsoreToLowerCamel(hit.getSourceAsMap()));
                resultMap.putAll(getHighlightField(hit));
                resList.add(resultMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resList;
    }

    /**
     * 下划线转驼峰
     *
     * @param resourceMap
     * @return
     */
    private Map<String, Object> convertUpperUndercsoreToLowerCamel(Map<String, Object> resourceMap) {
        Map<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : resourceMap.entrySet()) {
            resultMap.put(convertUpperUndercsoreToLowerCamel(entry.getKey()), entry.getValue());
        }
        return resultMap;
    }

    private String convertUpperUndercsoreToLowerCamel(String key) {
        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile("_(\\w)");
        Matcher m = p.matcher(key);
        while (m.find()) {
            m.appendReplacement(sb, m.group(1).toUpperCase());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 提取高亮信息
     *
     * @param hit
     * @return
     */
    private Map<String, String> getHighlightField(SearchHit hit) {
        Map<String, String> highlightMap = new HashMap<>();
        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        for (Map.Entry<String, HighlightField> entry : highlightFields.entrySet()) {
            HighlightField highlight = entry.getValue();
            Text[] fragments = highlight.fragments();
            String fragmentString = fragments[0].string();
            highlightMap.put(entry.getKey(), fragmentString);
        }
        return highlightMap;
    }

    private Map<String, Object> getBaseInfoMap(SearchResponse sResponse, EsCriteria criteria) {
        Map<String, Object> baseInfo = new HashMap<>();
        if (sResponse == null) return baseInfo;
        baseInfo.put("totalHits", sResponse.getHits().getTotalHits());
        baseInfo.put("currentDate", DateUtils.formatDate(new Date(), "yyyy/MM/dd HH:mm:ss"));
        baseInfo.put("pageSize", criteria.getPageSize());
        baseInfo.put("pageIndex", criteria.getPageIndex());
        baseInfo.put("totalPage", criteria.getTotalPage(
                Integer.parseInt(String.valueOf(baseInfo.get("totalHits")))));
        return baseInfo;
    }

    public List<Map<String, Object>> queryForList(EsCriteria criteria) {
        try {
            SearchResponse sResponse = queryForResponse(criteria);
            List<Map<String, Object>> resultList = getHitList(sResponse);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 普通查询
     *
     * @param criteria
     * @return
     * @throws Exception
     */
    public Map<String, Object> queryWithBaseInfo(EsCriteria criteria){
        Map<String, Object> resultMap = new HashMap<>();
        try {
            SearchResponse sResponse = queryForResponse(criteria);
            resultMap.put("baseInfoMap", getBaseInfoMap(sResponse, criteria));
            resultMap.put("resultList", getHitList(sResponse));
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultMap;
    }


    /**
     * 加入打分脚本查询
     * @param criteria
     * @param script
     * @param mode
     * @return
     */
    public Map<String, Object> functionQueryForResponse(EsCriteria criteria, String script, CombineFunction mode) {
        if(StringUtils.isEmpty(script)){
            throw new IllegalArgumentException("打分脚本不能为空");
        }
        if(mode == null){
            throw new IllegalArgumentException("打分模式不能为空");
        }
        if(criteria == null){
            throw new IllegalArgumentException("搜索条件不能为空");
        }

        Map<String, Object> resultMap = new HashMap<>();
        QueryBuilder builder = QueryBuilders.functionScoreQuery(criteria.getFilterBuilder() ,ScoreFunctionBuilders.scriptFunction(script)).boostMode(mode);
        try {
            SearchResponse sResponse = this.queryForResponse(criteria, builder);
            resultMap.put("baseInfoMap", getBaseInfoMap(sResponse, criteria));
            resultMap.put("resultList", getHitList(sResponse));
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultMap;
    }


    /**
     * 聚合查询
     *
     * @param criteria
     * @param includeSource 是否返回搜索结果
     * @return
     * @throws Exception
     */
    public Map<String, Object> queryWithAggregation(EsCriteria criteria, boolean includeSource) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> aggMap = new HashMap<>();
        try {
            SearchResponse sResponse = queryForResponse(criteria);
            Aggregations aggregations = sResponse.getAggregations();
            aggMap = this.getAggregationMap(aggregations);
            if (includeSource) {
                resultMap.put("resultList", getHitList(sResponse));
                resultMap.put("baseInfoMap", getBaseInfoMap(sResponse, criteria));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        resultMap.put("aggregationMap", aggMap);
        return resultMap;
    }

    /**
     * 提取聚合信息
     *
     * @param aggregations
     * @return
     */
    private Map<String, Object> getAggregationMap(Aggregations aggregations) {
        return EsAggregations.getAggregationMap(aggregations);
    }


    public Map<String, Object> queryWithAggregation(EsCriteria criteria) throws Exception {
        return this.queryWithAggregation(criteria, true);
    }

    /**
     * 批量更新搜索引擎数据
     *
     * @param criteria
     * @return
     * @throws Exception
     */
    public BulkResponse updateInBulk(EsCriteria criteria) throws Exception {
        BulkRequest bulkRequest = new BulkRequest();
        List<Tuple<String, Script>> updateReqList = criteria.getUpdateScripts();
        for (Tuple<String, Script> tuple : updateReqList) {
            UpdateRequest request = new UpdateRequest(properties.getIndex(), properties.getType(), tuple.v1());
            request.script(tuple.v2());
            bulkRequest.add(request);
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        return bulkResponse;
    }

    public EsSettingsProperties getProperties() {
        return properties;
    }

    public void setProperties(EsSettingsProperties properties) {
        this.properties = properties;
    }

}
