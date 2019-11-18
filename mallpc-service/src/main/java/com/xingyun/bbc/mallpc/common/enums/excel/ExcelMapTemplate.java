package com.xingyun.bbc.mallpc.common.enums.excel;

import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author nick
 * @ClassName: ExcelMapTemplate
 * @Description:
 * @date 2019年08月22日 15:33:37
 */
public class ExcelMapTemplate {

    /**
     * @author nick
     * @date 2019-08-22
     * @Description :  head -> field
     * @version 1.0.0
     */

    public List<ExcelTemplate> relationList=new ArrayList<>();


    public List<String> getFields() {
        if (relationList.size() > 0) {
            return relationList.parallelStream().map(ExcelTemplate::getFieldName).collect(toList());
        } else {
            throw new BizException(ResultStatus.NOT_IMPLEMENTED);
        }
    }

    public List<String> getHeads() {
        if (relationList.size() > 0) {
            return relationList.parallelStream().map(ExcelTemplate::getHeadName).collect(toList());
        } else {
            throw new BizException(ResultStatus.NOT_IMPLEMENTED);
        }
    }

    public String getField(String headName) {
        if (relationList.size() > 0) {
            return relationList.parallelStream().filter(template -> template.getHeadName().equals(headName)).collect(toList()).get(0).getFieldName();
        } else {
            throw new BizException(ResultStatus.NOT_IMPLEMENTED);
        }

    }
}
