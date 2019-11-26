package com.xingyun.bbc.mallpc.model.dto;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 分页请求参数
 *
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-19
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class PageDto extends BaseDto implements Serializable {

    private static final long serialVersionUID = -7411737604429433453L;

    private static final Integer MAX_PAGE_SIZE = Integer.MAX_VALUE;

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码")
    private Integer currentPage = 1;

    /**
     * 页容量
     */
    @ApiModelProperty(value = "页容量")
    private Integer pageSize = 20;

    public Integer getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        if (currentPage != null && currentPage.intValue() > 0) {
            this.currentPage = currentPage;
        }
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(Integer pageSize) {
        if (pageSize == null || pageSize.intValue() <= 0) {
            return;
        }
        if (pageSize.intValue() > MAX_PAGE_SIZE.intValue()) {
            pageSize = MAX_PAGE_SIZE;
        }
        this.pageSize = pageSize;
    }

}
