package com.xingyun.bbc.common.elasticsearch.config;

import org.apache.commons.collections.MapUtils;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.SingleBucketAggregation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * ES聚合工具类
 */
public class EsAggregations {

    public static final String AGGREGATION_KEY_NAME = "key";
    public static final String SUBAGGREGATION_NAME = "subaggregation";

    /**
     * 为 MultiBucketsAggregation 提取聚合信息
     * @param terms
     * @return
     */
    public static List<Map<String, Object>> getAggregationList(MultiBucketsAggregation multiBucketsAggregation){
        List<? extends MultiBucketsAggregation.Bucket> buckets = multiBucketsAggregation.getBuckets();
        List<Map<String, Object>> bucketLists = new LinkedList<>();
        //迭代bucket list, 提取对应聚合信息
        for (MultiBucketsAggregation.Bucket bucket : buckets) {
            Map<String, Object> map = new HashMap<>();
            map.put(AGGREGATION_KEY_NAME, bucket.getKey());
            //提取bucket下子聚合信息
            Map<String, Object> subAggregationMap = getAggregationMap(bucket.getAggregations());
            if(MapUtils.isNotEmpty(subAggregationMap)){
                map.put(SUBAGGREGATION_NAME, subAggregationMap);
            }
            bucketLists.add(map);
        }
        return bucketLists;
    }

    /**
     * 为 SingleBucketAggregation 提取聚合信息
     * @param nested
     * @return
     */
    public static List<Map<String, Object>> getAggregationList(SingleBucketAggregation singleBucketAggregation){
        //提取bucket下子聚合信息
        Map<String, Object> subAggregationMap = getAggregationMap(singleBucketAggregation.getAggregations());
        List<Map<String, Object>> bucketLists = new LinkedList<>();
        bucketLists.add(subAggregationMap);
        return bucketLists;
    }

    /**
     * 提取聚合信息, 封装成Map<String, Object>返回
     * @param aggregations
     * @return
     */
    public static Map<String, Object> getAggregationMap(Aggregations aggregations) {
        Map<String, Object> resultMap = new HashMap<>();
        //入参为空则返回空Map
        if(MapUtils.isEmpty(aggregations.getAsMap())){
            return resultMap;
        }
        //迭代所有聚合,依次提取对应信息
        Map<String, Aggregation> aggMap = aggregations.getAsMap();
        aggMap.forEach((str, Agg) -> {
            List<Map<String, Object>> aggList;
            //根据聚合类型调用相应处理方法
            if(Agg instanceof MultiBucketsAggregation){
                aggList = getAggregationList((MultiBucketsAggregation) Agg);
            }else if(Agg instanceof SingleBucketAggregation){
                aggList = getAggregationList((SingleBucketAggregation) Agg);
            }else {
                throw new UnsupportedOperationException("不支持该类型的聚合");
            }
            resultMap.put(str, aggList);
        });
        return resultMap;
    }


}
