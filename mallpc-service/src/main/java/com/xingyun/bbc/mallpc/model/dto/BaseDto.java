package com.xingyun.bbc.mallpc.model.dto;

import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-24
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class BaseDto implements Serializable {

    private static final long serialVersionUID = -3969190777947233434L;

    /**
     * 查询类型
     */
    @ApiModelProperty(value = "查询类型")
    private Integer searchType;

    /**
     * 查询关键字
     */
    @ApiModelProperty(value = "查询关键字")
    private String keyword;

    public Integer getSearchType() {
        return searchType;
    }

    public void setSearchType(Integer searchType) {
        this.searchType = searchType;
    }

    public String getKeyword() {
        return StringUtils.replace(StringEscapeUtils.escapeSql(StringUtils.trim(this.keyword)), "%", "\\%");
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
