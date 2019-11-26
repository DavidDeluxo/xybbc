package com.xingyun.bbc.mallpc.common.utils;

import com.google.common.collect.Lists;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.model.dto.PageDto;
import com.xingyun.bbc.mallpc.model.vo.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-19
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Component
public class PageHelper {

    @Autowired
    private DozerHolder dozerHolder;

    private PageHelper() {
    }

    /**
     * 分页列表数据po->vo
     *
     * @param totalCount
     * @param source
     * @param destinationClass
     * @param pageDto
     * @param <T>
     * @return
     */
    public <T> PageVo<T> convert(Integer totalCount, List source, Class<T> destinationClass, PageDto pageDto) {
        if (totalCount == 0) {
            return new PageVo<>(0, pageDto.getCurrentPage(), pageDto.getPageSize(), Lists.newArrayList());
        }
        return new PageVo(totalCount, pageDto.getCurrentPage(), pageDto.getPageSize(), dozerHolder.convert(source, destinationClass));
    }

    public <T> PageVo<T> convert(Integer totalCount, List list, PageDto pageDto) {
        if (totalCount == 0) {
            return new PageVo<>(0, pageDto.getCurrentPage(), pageDto.getPageSize(), Lists.newArrayList());
        }
        return new PageVo(totalCount, pageDto.getCurrentPage(), pageDto.getPageSize(), list);
    }

    /**
     * @Description :  条件查询为条数为0时返回空结果
     * @version 1.0.0
     */
    public <T> Result<PageVo<T>> emptyResult(PageDto pageDto,Class<T> clz){
        return Result.success(new PageVo(0, pageDto.getCurrentPage(), pageDto.getPageSize(), Collections.EMPTY_LIST));
    }

    /**
     * @author pengaoluo
     * @Description :  条件查询为条数为0时返回空结果
     * @version 1.0.0
     */
    public <T> Result<PageVo<T>> emptyResult(PageDto pageDto){
        return Result.success(new PageVo<T>(0, pageDto.getCurrentPage(), pageDto.getPageSize(), Collections.EMPTY_LIST));
    }
}
