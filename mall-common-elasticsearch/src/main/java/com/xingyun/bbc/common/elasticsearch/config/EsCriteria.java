package com.xingyun.bbc.common.elasticsearch.config;


import com.xingyun.bbc.common.elasticsearch.config.autobuild.EsMark;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.index.query.*;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

@Component
public class EsCriteria {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_PAGE_INDEX = 1;
    private static final int DEFAULT_START_INDEX = 0;
    private static final String KEYWORD_FIELD_SUFFIX = ".keyword";
    private static final String PINYIN_FIELD_SUFFIX = ".pinyin";
    private static final String FIELD_SEPERATOR = ",";
    public static final String BOOST_SEPERATOR = "^";
    public static final String BOOST_SEPERATOR_REGEX = "\\^";
    private static final String UPDATE_SCRIPT_COMPONENT = "ctx._source.? = params.?";
//    private static final String UPDATE_SCRIPT_ARRAY_APPEND = "if (!ctx._source.?.contains(params.?)){ctx._source.?.add(params.?)}";
    private static final String UPDATE_SCRIPT_ARRAY_APPEND = "ctx._source.?.add(params.?)";
    private static final String UPDATE_SCRIPT_PLACEHOLDER = "?";

    private BoolQueryBuilder filterBuilder = QueryBuilders.boolQuery();

    private Map<String, AggregationBuilder> aggBuilders = new HashMap<>();
    private AggregationBuilder currentAggregationBuilder = null;
    private SortBuilder sortBuilder = null;

    private HighlightBuilder highlightBuilder = null;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private int pageIndex = DEFAULT_PAGE_INDEX;
    private int startIndext = DEFAULT_START_INDEX;
    private String[] includeFields = null;
    private String[] excludeFields = null;
    private List<Tuple<String, Script>> updateScripts = new ArrayList<>();

    private List<SortBuilder> sorts = new ArrayList<>();


    public int getStartIndext() {
        return startIndext;
    }

    public void setStartIndext(int startIndext) {
        this.startIndext = startIndext;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public String[] getIncludeFields() {
        return includeFields;
    }

    public void setIncludeFields(String[] includeFields) {
        this.includeFields = includeFields;
    }

    public String[] getExcludeFields() {
        return excludeFields;
    }

    public void setExcludeFields(String[] excludeFields) {
        this.excludeFields = excludeFields;
    }

    public BoolQueryBuilder getFilterBuilder() {
        return filterBuilder;
    }

    public void setFilterBuilder(BoolQueryBuilder filterBuilder) {
        this.filterBuilder = filterBuilder;
    }


    public Map<String, AggregationBuilder> getAggBuilders() {
        return aggBuilders;
    }

    public void setAggBuilders(Map<String, AggregationBuilder> aggBuilders) {
        this.aggBuilders = aggBuilders;
    }

    public SortBuilder getSortBuilder() {
        return sortBuilder;
    }

    public void setSortBuilder(SortBuilder sortBuilder) {
        this.sortBuilder = sortBuilder;
    }


    public List<SortBuilder> getSorts() {
        return sorts;
    }

    public void setSorts(List<SortBuilder> sorts) {
        this.sorts = sorts;
    }


    public List<Tuple<String, Script>> getUpdateScripts() {
        return updateScripts;
    }

    public void setUpdateScripts(List<Tuple<String, Script>> updateScripts) {
        this.updateScripts = updateScripts;
    }

    public EsCriteria page(int pageIndex, int pageSize) {
        if (pageIndex > 0) {
            this.pageIndex = pageIndex;
        }
        if (pageSize > 0) {
            this.pageSize = pageSize;
        }
        this.startIndext = (this.pageIndex - 1) * this.pageSize;
        return this;
    }

    public EsCriteria page(Object pageIndex, Object pageSize) {
        int index = -1;
        int size = -1;

        if (pageIndex != null) {
            index = Integer.parseInt(String.valueOf(pageIndex));
        }

        if (pageSize != null) {
            size = Integer.parseInt(String.valueOf(pageSize));
        }
        page(index, size);
        return this;
    }

    public EsCriteria must(String fieldname, String fieldvalue) {
        String[] fieldValueArr = fieldvalue.split(FIELD_SEPERATOR);
        filterBuilder.must(QueryBuilders.termsQuery(fieldname, fieldValueArr));
        return this;
    }


    public EsCriteria OrMust(Object fieldname, List<Object> fieldvalue) {
        if(!CollectionUtils.isEmpty(fieldvalue ) && fieldname != null){
            DisMaxQueryBuilder disMaxQuerys = QueryBuilders.disMaxQuery();
            for(Object value : fieldvalue){
                disMaxQuerys.add(QueryBuilders.termsQuery(String.valueOf(fieldname), value));
            }
            filterBuilder.must(disMaxQuerys);
        }
        return this;
    }

    public EsCriteria must(Object fieldname, Object fieldvalue) {
        if (fieldname != null && fieldvalue != null) {
            must(String.valueOf(fieldname), String.valueOf(fieldvalue));
        }
        return this;
    }

    public EsCriteria must(String fieldname, int fieldvalue) {
        filterBuilder.must(QueryBuilders.termQuery(fieldname, fieldvalue));
        return this;
    }

    public EsCriteria mustText(String fieldname, String fieldvalue) {
        filterBuilder.must(QueryBuilders.termQuery(FieldNameParser_KeyWord(fieldname), fieldvalue));
        return this;
    }

    public EsCriteria mustText(Object fieldname, Object fieldvalue) {
        if (fieldname != null && fieldvalue != null) {
            String s_filedName = String.valueOf(fieldname);
            filterBuilder.must(QueryBuilders.termQuery(FieldNameParser_KeyWord(s_filedName), fieldvalue));
        }
        return this;
    }

    public EsCriteria mustText(String fieldname, int fieldvalue) {
        filterBuilder.must(QueryBuilders.termQuery(FieldNameParser_KeyWord(fieldname), fieldvalue));
        return this;
    }

    public EsCriteria exist(String fieldname) {
        filterBuilder.must(QueryBuilders.existsQuery(fieldname));
        return this;
    }

    public EsCriteria mustNot(String fieldname, String fieldvalue) {
        filterBuilder.mustNot(QueryBuilders.termQuery(fieldname, fieldvalue));
        return this;
    }

    public EsCriteria mustNot(String fieldname, int fieldvalue) {
        filterBuilder.mustNot(QueryBuilders.termQuery(fieldname, fieldvalue));
        return this;
    }

    public EsCriteria mustNotText(String fieldname, String fieldvalue) {
        filterBuilder.mustNot(QueryBuilders.termQuery(FieldNameParser_KeyWord(fieldname), fieldvalue));
        return this;
    }

    public EsCriteria mustNotText(String fieldname, int fieldvalue) {
        filterBuilder.mustNot(QueryBuilders.termQuery(FieldNameParser_KeyWord(fieldname), fieldvalue));
        return this;
    }

    public EsCriteria range(String fromFieldName, String toFieldName, Object fromValue, Object toValue) {
        rangeFrom(fromFieldName, fromValue);
        rangeTo(toFieldName, toValue);
        return this;
    }

    public EsCriteria range(String fieldName, Object fromValue, Object toValue) {
        rangeFrom(fieldName, fromValue);
        rangeTo(fieldName, toValue);
        return this;
    }

    public EsCriteria rangeFrom(String fromFieldName, Object fromValue) {
        RangeQueryBuilder rangeQuery = null;
        if (fromValue != null && fromFieldName != null) {
            rangeQuery = QueryBuilders.rangeQuery(fromFieldName).from(fromValue);
            filterBuilder.filter(rangeQuery);
        }
        return this;
    }

    public EsCriteria rangeTo(String toFieldName, Object toValue) {
        RangeQueryBuilder rangeQuery = null;
        if (toValue != null && toFieldName != null) {
            rangeQuery = QueryBuilders.rangeQuery(toFieldName).to(toValue);
            filterBuilder.filter(rangeQuery);
        }
        return this;
    }


    /**
     * 构建全文搜索条件
     *
     * @param keyWord
     * @param fieldNames 纳入搜索范围的目标字段数组, 字段名可带增益, 通过"^"进行分割,
     *                   例："fitem_name^3.5" 为字段"fitem_name"增加3.5倍的增益, 默认
     *                   增益为1
     * @return
     */
    public EsCriteria fullTextMatch(String keyWord, String... fieldNames) {
        if (!Strings.isNullOrEmpty(keyWord) || fieldNames.length == 0) {
            List<String> fieldNameList = Arrays.asList(fieldNames);
            //过滤增益后的字段数组
            List<String> true_file_name_list = new LinkedList<>();
            //增益Map
            Map<String, Float> boostMap = new HashMap<>();
            //字段名过滤增益,并生成boostMap
            for (String fieldName : fieldNameList) {
                String[] list = fieldName.split(BOOST_SEPERATOR_REGEX);
                String true_field_name = list[0];
                true_file_name_list.add(true_field_name);
                if (list.length > 1 && NumberUtils.isCreatable(list[1])) {
                    boostMap.put(true_field_name, new Float((list[1])));
                }
            }
            String[] true_fieldNames = new String[true_file_name_list.size()];
            true_fieldNames = true_file_name_list.toArray(true_fieldNames);
            //全文搜索条件
            MultiMatchQueryBuilder multyMatch = QueryBuilders.multiMatchQuery(keyWord, true_fieldNames).type(MultiMatchQueryBuilder.Type.CROSS_FIELDS);
            //增益条件
            multyMatch.fields(boostMap);
            //前缀匹配条件
            MultiMatchQueryBuilder preMultyMatch = QueryBuilders.multiMatchQuery(keyWord, true_fieldNames).type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX);
            filterBuilder.should(multyMatch).should(preMultyMatch).minimumShouldMatch(1);
        }
        return this;
    }


    public EsCriteria fullTextMatch(Object keyWord, Object fieldNames) {
        if (keyWord != null && fieldNames != null) {
            fullTextMatch(String.valueOf(keyWord), String.valueOf(fieldNames).split(FIELD_SEPERATOR));
        }
        return this;
    }

    public EsCriteria _source(String[] includeFields, String[] excludeFields) {
        this.includeFields = includeFields;
        this.excludeFields = excludeFields;
        return this;
    }


    public EsCriteria sortBy(String fieldName, SortOrder order) {
        SortBuilder sortBuilder = SortBuilders.fieldSort(fieldName).order(order);
        sorts.add(sortBuilder);
        return this;
    }

    public EsCriteria sortBy(String fieldName, Object order) {
        if (StringUtils.isEmpty(fieldName) || order == null || StringUtils.isEmpty(String.valueOf(order))) {
            return this;
        }
        SortOrder search_order = SortOrder.DESC;
        String param_order = String.valueOf(order);
        if (param_order.equals(SortOrder.ASC.toString())) {
            search_order = SortOrder.ASC;
        }
        SortBuilder sortBuilder = SortBuilders.fieldSort(fieldName).order(search_order);
        sorts.add(sortBuilder);
        return this;
    }

    public EsCriteria _sourceInclude(String includeFieldsString) {
        String[] includeFields = includeFieldsString.split(FIELD_SEPERATOR);
        _source(includeFields, null);
        return this;
    }

    public EsCriteria _sourceInclude(Object includeFieldsString) {
        if (includeFieldsString != null) {
            String s_includeFields = String.valueOf(includeFieldsString);
            _sourceInclude(s_includeFields);
        }
        return this;
    }


    public EsCriteria scriptSort(Script script, ScriptSortBuilder.ScriptSortType scriptSortType, SortOrder order) {
        SortBuilder sortBuilder = SortBuilders.scriptSort(script, scriptSortType).order(order);
        sorts.add(sortBuilder);
        return this;
    }

    public void addSort(String fieldName, SortOrder order) {
        SortBuilder sortBuilder = SortBuilders.fieldSort(fieldName).order(order);
        sorts.add(sortBuilder);
    }


    public EsCriteria sortByScore(SortOrder order) {
        SortBuilder sortBuilder = SortBuilders.scoreSort().order(order);
        sorts.add(sortBuilder);
        return this;
    }

    private static String FieldNameParser_KeyWord(String origin) {
        if (Strings.isNullOrEmpty(origin)) {
            return null;
        }
        if (origin.endsWith(KEYWORD_FIELD_SUFFIX)) {
            return origin;
        }
        return origin + KEYWORD_FIELD_SUFFIX;
    }

    private static String[] FieldNameParser_PinYin(String... fieldNames) {
        List<String> list = new ArrayList<>();

        for (String origin : fieldNames) {
            if (Strings.isNullOrEmpty(origin)) {
                continue;
            }
            if (origin.endsWith(PINYIN_FIELD_SUFFIX)) {
                list.add(origin);
            }
            list.add(origin + PINYIN_FIELD_SUFFIX);
        }
        String[] ary = (String[]) list.toArray(new String[list.size()]);
        return ary;
    }

    public Integer getTotalPage(int totalHits) {
        Integer total = new Integer(0);
        if (totalHits < 0) return total;

        if (totalHits % pageSize == 0) {
            total = totalHits / pageSize;
        } else {
            total = totalHits / pageSize + 1;
        }

        return total;
    }

    /**
     * 构建聚合条件
     *
     * @param aggName   自定义聚合名称
     * @param fieldName 聚合字段
     */
    public EsCriteria termAggregate(String aggName, String fieldName) {
        if (aggName != null && fieldName != null) {
            AggregationBuilder aggBuilder =
                    AggregationBuilders.terms(aggName)
                            .field(fieldName)
                            .order(BucketOrder.count(false));
            this.aggBuilders.put(aggName, aggBuilder);
            currentAggregationBuilder = aggBuilder;
        }
        return this;
    }

    /**
     * 构建子聚合条件
     *
     * @param aggName   自定义聚合名称
     * @param fieldName 聚合字段
     */
    public EsCriteria subAggregate(String aggName, String fieldName) {
        if (currentAggregationBuilder != null && aggName != null && fieldName != null) {
            AggregationBuilder aggBuilder = AggregationBuilders.terms(aggName).field(fieldName);
            currentAggregationBuilder.subAggregation(aggBuilder);
            currentAggregationBuilder = aggBuilder;
        }
        return this;
    }

    public EsCriteria highlightFields(String... fieldNames) {
        this.highlightBuilder = new HighlightBuilder();
        for (String fieldName : fieldNames) {
            HighlightBuilder.Field hightField = new HighlightBuilder.Field(fieldName);
            highlightBuilder.field(hightField);
        }
        return this;
    }

    public HighlightBuilder getHighlightBuilder() {
        return highlightBuilder;
    }

    public void setHighlightBuilder(HighlightBuilder highlightBuilder) {
        this.highlightBuilder = highlightBuilder;
    }

    /**
     * 扫描入参类属性注解, 自动构建搜索条件
     *
     * @param Object
     * @return EsCriteria
     * @throws Exception
     */
    public EsCriteria autoBuild(Object param) throws Exception {
        //获取类信息
        Class<?> cl = param.getClass();
        //迭代类所有属性提取注解
        for (Field field : cl.getDeclaredFields()) {
            EsMark mark = field.getAnnotation(EsMark.class);
            if (mark != null) {
                //更改私有属性获取权限
                field.setAccessible(true);
                //提取属性值
                Object fieldValue = field.get(param);
                //调用注解关联方法构建条件
                mark.policy().build(this, mark, fieldValue);
            }
        }
        return this;
    }

    public static EsCriteria build(Object param) {
        EsCriteria criteria = new EsCriteria();
        if(param != null){
            try {
                criteria.autoBuild(param);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return criteria;
    }


    public EsCriteria addUpdateRequest(Map<String, Object> parameters) throws Exception {

        if (parameters == null || (!parameters.containsKey("documentId"))) {
            return this;
        }

        String documentId = String.valueOf(parameters.get("documentId"));
        parameters.remove("documentId");
        for (Entry<String, Object> entry : parameters.entrySet()) {
            Map<String, Object> param = new HashMap<>(2);
//			param.put(entry.getKey(), Integer.parseInt(String.valueOf(entry.getValue())));
            param.put(entry.getKey(), entry.getValue());
            Script inline = new Script(ScriptType.INLINE, "painless",
                    getScript_singleFieldUpdate(entry), param);
            Tuple<String, Script> tuple = new Tuple<String, Script>(documentId, inline);
            updateScripts.add(tuple);
        }
        return this;
    }


    public EsCriteria addUpdateRequest(String documentId, Map<String, Object> parameters){
        if(StringUtils.isEmpty(documentId) || MapUtils.isEmpty(parameters)){
            return this;
        }
        for (Entry<String, Object> entry : parameters.entrySet()) {
            Map<String, Object> param = new HashMap<>(2);
            param.put(entry.getKey(), entry.getValue());
            Script inline = new Script(ScriptType.INLINE, "painless",
                    getScript_singleFieldUpdate(entry), param);
            Tuple<String, Script> tuple = new Tuple<String, Script>(documentId, inline);
            updateScripts.add(tuple);
        }
        return this;
    }

    private String getScript_singleFieldUpdate(Entry<String, Object> fieldValuePair) {
        String script = StringUtils.replace(UPDATE_SCRIPT_ARRAY_APPEND, UPDATE_SCRIPT_PLACEHOLDER, fieldValuePair.getKey());
        return script;
    }


}
