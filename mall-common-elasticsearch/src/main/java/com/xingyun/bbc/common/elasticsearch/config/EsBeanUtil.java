package com.xingyun.bbc.common.elasticsearch.config;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class EsBeanUtil {

    private static final Pattern ID_PATTERN = Pattern.compile(".*Id");
    private static final Pattern NAME_PATTERN = Pattern.compile(".*Name");
    private static final Pattern SUB_PAIR_LIST_PATTERN = Pattern.compile(".*SubPairList");

    private static final Map<Class<?>, EsBeanUtil.BeanIntrospectionInfo> cachedBeanIntrospectionInfo = new ConcurrentHashMap<>();

    @Data
    private static class BeanIntrospectionInfo {

        private Field idField;

        private Field nameField;

        Field nestedPairField;

        Class<?> nestedBeanClass;

        void setValueForIdField(Object object, Object value) throws Exception {
            idField.setAccessible(true);
            idField.set(object, value);
        }

        void setValueForNameField(Object object, Object value) throws Exception {
            nameField.setAccessible(true);
            nameField.set(object, value);
        }

        void setValueForNestedPairField(Object object, Object value) throws Exception {
            nestedPairField.setAccessible(true);
            nestedPairField.set(object, value);
        }
    }

    private static void cacheBeanInfo(Class<?> clazz) {
        EsBeanUtil.BeanIntrospectionInfo beanInfo = new EsBeanUtil.BeanIntrospectionInfo();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {

            String fieldName = field.getName();
            if (Objects.isNull(beanInfo.getIdField()) && ID_PATTERN.matcher(fieldName).matches()) {
                beanInfo.setIdField(field);
            }
            if (Objects.isNull(beanInfo.getNameField()) && NAME_PATTERN.matcher(fieldName).matches()) {
                beanInfo.setNameField(field);
            }

            if (Objects.isNull(beanInfo.getNestedPairField()) && SUB_PAIR_LIST_PATTERN.matcher(fieldName).matches()) {
                ParameterizedType pType = (ParameterizedType) field.getGenericType();
                Class<?> actualType = (Class) pType.getActualTypeArguments()[0];
                beanInfo.setNestedPairField(field);
                beanInfo.setNestedBeanClass(actualType);
            }

        }
        if (beanInfo.getIdField() == null) {
            throw new RuntimeException("û���ҵ�id����");
        }
        if (beanInfo.getNameField() == null) {
            throw new RuntimeException("û���ҵ���������");
        }
        cachedBeanIntrospectionInfo.put(clazz, beanInfo);
    }


    /**
     * ���þۺ�id,�Ӿۺ�id����,��ȡid-���ƶ�, ��ȡbucketList����ľۺ���Ϣ, ������Vo
     * @param clazz
     * @param bucketList
     * @param <T>
     * @return
     */
    public static <T> List<T> getValueObjectList(Class<T> clazz, List<Map<String, Object>> bucketList) {
        if (Objects.isNull(clazz)) {
            throw new IllegalArgumentException("Vo���Ͳ���Ϊ��");
        }
        if (Objects.isNull(cachedBeanIntrospectionInfo.get(clazz))) {
            cacheBeanInfo(clazz);
        }
        List<T> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(bucketList)) {
            return resultList;
        }
        EsBeanUtil.BeanIntrospectionInfo info = cachedBeanIntrospectionInfo.get(clazz);
        try {
            for (Map<String, Object> idMap : bucketList) {
                //��ȡid��Ϣ
                T valueObject = clazz.newInstance();
                Object idValue = idMap.get(EsManager.AGGREGATION_KEY_NAME);
                info.setValueForIdField(valueObject, Integer.parseInt(String.valueOf(idValue)));
                //��ȡ������Ϣ
                Map<String, Object> nameMap = (Map<String, Object>) idMap.get(EsManager.SUBAGGREGATION_NAME);
                if (MapUtils.isNotEmpty(nameMap)) {
                    Object[] nameMapValues = nameMap.values().toArray();
                    List<Map<String, Object>> nameList = (List<Map<String, Object>>) nameMapValues[0];
                    if (CollectionUtils.isNotEmpty(nameList)) {
                        Map<String, Object> AggMap = nameList.get(0);
                        Object nameValue = nameList.get(0).get(EsManager.AGGREGATION_KEY_NAME);
                        info.setValueForNameField(valueObject, String.valueOf(nameValue));
                        //��ȡ�Ӿۺ���Ϣ
                        if (!Objects.isNull(info.getNestedPairField())) {
                            Map<String, Object> subAggMap = (Map<String, Object>) AggMap.get(EsManager.SUBAGGREGATION_NAME);
                            List<Map<String, Object>> subAggList = (List<Map<String, Object>>) subAggMap.values().toArray()[0];
                            info.setValueForNestedPairField(valueObject, getValueObjectList(info.getNestedBeanClass(), subAggList));
                        }
                    }
                }
                resultList.add(valueObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("�ڲ����ô���");
        }
        return resultList;
    }


}
