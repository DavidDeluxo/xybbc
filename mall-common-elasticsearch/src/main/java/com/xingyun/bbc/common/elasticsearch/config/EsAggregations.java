package com.xingyun.bbc.common.elasticsearch.config;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.SingleBucketAggregation;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * ES聚合工具类
 */
public class EsAggregations {

    public static final String AGGREGATION_KEY_NAME = "key";
    public static final String SUBAGGREGATION_NAME = "subaggregation";

    private static final Pattern ID_PATTERN = Pattern.compile(".*Id");
    private static final Pattern NAME_PATTERN = Pattern.compile(".*Name");
    private static final Pattern SUB_PAIR_LIST_PATTERN = Pattern.compile(".*SubPairList");

    /**
     * 为 MultiBucketsAggregation 提取聚合信息
     *
     * @param terms
     * @return
     */
    public static List<Map<String, Object>> getAggregationList(MultiBucketsAggregation multiBucketsAggregation) {
        List<? extends MultiBucketsAggregation.Bucket> buckets = multiBucketsAggregation.getBuckets();
        List<Map<String, Object>> bucketLists = new LinkedList<>();
        //迭代bucket list, 提取对应聚合信息
        for (MultiBucketsAggregation.Bucket bucket : buckets) {
            Map<String, Object> map = new HashMap<>();
            map.put(AGGREGATION_KEY_NAME, bucket.getKey());
            //提取bucket下子聚合信息
            Map<String, Object> subAggregationMap = getAggregationMap(bucket.getAggregations());
            if (MapUtils.isNotEmpty(subAggregationMap)) {
                map.put(SUBAGGREGATION_NAME, subAggregationMap);
            }
            bucketLists.add(map);
        }
        return bucketLists;
    }

    /**
     * 为 SingleBucketAggregation 提取聚合信息
     *
     * @param nested
     * @return
     */
    public static List<Map<String, Object>> getAggregationList(SingleBucketAggregation singleBucketAggregation) {
        //提取bucket下子聚合信息
        Map<String, Object> subAggregationMap = getAggregationMap(singleBucketAggregation.getAggregations());
        List<Map<String, Object>> bucketLists = new LinkedList<>();
        bucketLists.add(subAggregationMap);
        return bucketLists;
    }

    /**
     * 提取聚合信息, 封装成Map<String, Object>返回
     *
     * @param aggregations
     * @return
     */
    public static Map<String, Object> getAggregationMap(Aggregations aggregations) {
        Map<String, Object> resultMap = new HashMap<>();
        //入参为空则返回空Map
        if (MapUtils.isEmpty(aggregations.getAsMap())) {
            return resultMap;
        }
        //迭代所有聚合,依次提取对应信息
        Map<String, Aggregation> aggMap = aggregations.getAsMap();
        aggMap.forEach((str, Agg) -> {
            List<Map<String, Object>> aggList;
            //根据聚合类型调用相应处理方法
            if (Agg instanceof MultiBucketsAggregation) {
                aggList = getAggregationList((MultiBucketsAggregation) Agg);
            } else if (Agg instanceof SingleBucketAggregation) {
                aggList = getAggregationList((SingleBucketAggregation) Agg);
            } else {
                throw new UnsupportedOperationException("不支持该类型的聚合");
            }
            resultMap.put(str, aggList);
        });
        return resultMap;
    }


    /**
     * 提取bucketList里面的聚合信息, 并生成Vo, 重载方法
     *
     * @param aggregationList
     * @param clazz
     * @param <T>
     * @param <U>
     * @return
     */
    public static <T, U> List<T> getNameIdPairs(List<Map<String, Object>> aggregationList, Class<T> clazz) {
        return getNameIdPairs(aggregationList, clazz, null);
    }


    /**
     * 采用聚合id,子聚合id名称,获取id-名称对, 提取bucketList里面的聚合信息, 并生成Vo
     *
     * @param aggregationList 单个聚合返回的bucket列表
     * @param clazz
     * @param clazz2
     * @param <T>
     * @param <U>
     * @return
     */
    public static <T, U> List<T> getNameIdPairs(List<Map<String, Object>> aggregationList, Class<T> clazz, Class<U> clazz2) {
        //id字段
        Field id_field = null;
        //名称字段
        Field name_field = null;
        //嵌套集合字段
        Field sub_pair_list_field = null;
        Field[] fields = clazz.getDeclaredFields();
        //扫描Vo字段
        for (Field field : fields) {
            String fieldName = field.getName();
            if (id_field == null && ID_PATTERN.matcher(fieldName).matches()) {
                id_field = field;
            }
            if (name_field == null && NAME_PATTERN.matcher(fieldName).matches()) {
                name_field = field;
            }
            if (sub_pair_list_field == null && SUB_PAIR_LIST_PATTERN.matcher(fieldName).matches()) {
                ParameterizedType pType = (ParameterizedType) field.getGenericType();
                Type actualType = pType.getActualTypeArguments()[0];
                if (actualType.getTypeName().equals(clazz2.getTypeName())) {
                    sub_pair_list_field = field;
                }
            }
        }

        //校验Vo字段是否存在
        if (id_field == null) {
            throw new RuntimeException("没有找到id属性");
        }
        if (name_field == null) {
            throw new RuntimeException("没有找到名称属性");
        }
        //获取修改权限
        id_field.setAccessible(true);
        name_field.setAccessible(true);
        //校验聚合信息是否为空
        List<T> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(aggregationList)) {
            return resultList;
        }
        try {
            //迭代bucket列表,提取信息
            for (Map<String, Object> idMap : aggregationList) {
                //创建Vo实例
                Object valueObject = clazz.newInstance();
                //id值
                Object idValue = idMap.get(EsManager.AGGREGATION_KEY_NAME);
                //校验id值类型
                if (!id_field.getType().equals(Integer.class)) {
                    throw new IllegalArgumentException();
                }
                //写入id值
                id_field.set(valueObject, Integer.parseInt(String.valueOf(idValue)));

                //id名称信息
                Map<String, Object> nameMap = (Map<String, Object>) idMap.get(EsManager.SUBAGGREGATION_NAME);
                if (MapUtils.isNotEmpty(nameMap)) {
                    Object[] nameMapValues = nameMap.values().toArray();
                    //默认单个id下只有一个名称
                    List<Map<String, Object>> nameList = (List<Map<String, Object>>) nameMapValues[0];
                    if (CollectionUtils.isNotEmpty(nameList)) {
                        Object nameValue = nameList.get(0).get(EsManager.AGGREGATION_KEY_NAME);
                        if (!name_field.getType().equals(String.class)) {
                            throw new IllegalArgumentException();
                        }
                        //写入id名称
                        name_field.set(valueObject, String.valueOf(nameValue));
                        //提取嵌套聚合信息
                        Map<String, Object> subAggMap = (Map<String, Object>) nameList.get(0).get(EsManager.SUBAGGREGATION_NAME);
                        if (MapUtils.isNotEmpty(subAggMap) && clazz2 != null && sub_pair_list_field != null) {
                            Object[] subAggArray = subAggMap.values().toArray();
                            List<Map<String, Object>> subAggList = (List<Map<String, Object>>) subAggArray[0];
                            List<U> subPairList = getNameIdPairs(subAggList, clazz2);
                            sub_pair_list_field.setAccessible(true);
                            sub_pair_list_field.set(valueObject, subPairList);
                        }
                    }
                }
                resultList.add((T) valueObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("内部调用错误");
        }
        return resultList;
    }


}
