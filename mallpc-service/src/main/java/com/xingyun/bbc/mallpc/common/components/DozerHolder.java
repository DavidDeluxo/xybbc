package com.xingyun.bbc.mallpc.common.components;

import com.google.common.collect.Lists;
import org.dozer.Mapper;
import org.dozer.util.MappingValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * dozer工具类
 *
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-17
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Component
public class DozerHolder {

    @Autowired
    private Mapper dozerMapper;

    private DozerHolder() {
    }

    /**
     * 集合转换
     *
     * @param source
     * @param destinationClass
     * @param <T>
     * @return
     */
    public <T> List<T> convert(List source, Class<T> destinationClass) {
        if (CollectionUtils.isEmpty(source)) {
            return Lists.newArrayList();
        }
        MappingValidator.validateMappingRequest(source, destinationClass);
        List target = new ArrayList(source.size());
        for (Object each : source) {
            target.add(dozerMapper.map(each, destinationClass));
        }
        return target;
    }

    /**
     *  @Description :转换单个对象
     *  @author :nick
     *  @Date :2019-08-19 23:37
     *  @param
     *  @return
     */

    public <T> T convert(Object source, Class<T> destinationClass) {
        MappingValidator.validateMappingRequest(source, destinationClass);
        return dozerMapper.map(source, destinationClass);
    }
}
