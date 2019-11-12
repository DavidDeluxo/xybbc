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
 * ES�ۺϹ�����
 */
public class EsAggregations {

    public static final String AGGREGATION_KEY_NAME = "key";
    public static final String SUBAGGREGATION_NAME = "subaggregation";

    /**
     * Ϊ MultiBucketsAggregation ��ȡ�ۺ���Ϣ
     *
     * @param terms
     * @return
     */
    public static List<Map<String, Object>> getAggregationList(MultiBucketsAggregation multiBucketsAggregation) {
        List<? extends MultiBucketsAggregation.Bucket> buckets = multiBucketsAggregation.getBuckets();
        List<Map<String, Object>> bucketLists = new LinkedList<>();
        //����bucket list, ��ȡ��Ӧ�ۺ���Ϣ
        for (MultiBucketsAggregation.Bucket bucket : buckets) {
            Map<String, Object> map = new HashMap<>();
            map.put(AGGREGATION_KEY_NAME, bucket.getKey());
            //��ȡbucket���Ӿۺ���Ϣ
            Map<String, Object> subAggregationMap = getAggregationMap(bucket.getAggregations());
            if (MapUtils.isNotEmpty(subAggregationMap)) {
                map.put(SUBAGGREGATION_NAME, subAggregationMap);
            }
            bucketLists.add(map);
        }
        return bucketLists;
    }

    /**
     * Ϊ SingleBucketAggregation ��ȡ�ۺ���Ϣ
     *
     * @param nested
     * @return
     */
    public static List<Map<String, Object>> getAggregationList(SingleBucketAggregation singleBucketAggregation) {
        //��ȡbucket���Ӿۺ���Ϣ
        Map<String, Object> subAggregationMap = getAggregationMap(singleBucketAggregation.getAggregations());
        List<Map<String, Object>> bucketLists = new LinkedList<>();
        bucketLists.add(subAggregationMap);
        return bucketLists;
    }

    /**
     * ��ȡ�ۺ���Ϣ, ��װ��Map<String, Object>����
     *
     * @param aggregations
     * @return
     */
    public static Map<String, Object> getAggregationMap(Aggregations aggregations) {
        Map<String, Object> resultMap = new HashMap<>();
        //���Ϊ���򷵻ؿ�Map
        if (MapUtils.isEmpty(aggregations.getAsMap())) {
            return resultMap;
        }
        //�������оۺ�,������ȡ��Ӧ��Ϣ
        Map<String, Aggregation> aggMap = aggregations.getAsMap();
        aggMap.forEach((str, Agg) -> {
            List<Map<String, Object>> aggList;
            //���ݾۺ����͵�����Ӧ������
            if (Agg instanceof MultiBucketsAggregation) {
                aggList = getAggregationList((MultiBucketsAggregation) Agg);
            } else if (Agg instanceof SingleBucketAggregation) {
                aggList = getAggregationList((SingleBucketAggregation) Agg);
            } else {
                throw new UnsupportedOperationException("��֧�ָ����͵ľۺ�");
            }
            resultMap.put(str, aggList);
        });
        return resultMap;
    }

}
