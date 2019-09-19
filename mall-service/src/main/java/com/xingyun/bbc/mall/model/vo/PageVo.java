package com.xingyun.bbc.mall.model.vo;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * @author penglu
 * @version 1.0.0
 * @date 2019-08-19
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class PageVo<E> implements Serializable {
    private static final long serialVersionUID = -3239957261248101448L;

    /**
     * 总记录数
     */
    @ApiModelProperty(value = "总记录数")
    private Integer totalCount;

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码")
    private Integer currentPage;

    /**
     * 页容量
     */
    @ApiModelProperty(value = "页容量")
    private Integer pageSize;

    /**
     * 总页数
     */
    @ApiModelProperty(value = "总页数")
    private Integer pageCount;

    /**
     * 是否有上一页
     */
    @ApiModelProperty(value = "是否有上一页")
    private boolean hasPrevious;

    /**
     * 是否有下一页
     */
    @ApiModelProperty(value = "是否有下一页")
    private boolean hasNext;

    /**
     * 分页数据
     */
    @ApiModelProperty(value = "分页数据")
    private List<E> list;

    public PageVo() {
    }

    public PageVo(Integer totalCount, Integer currentPage, Integer pageSize, List<E> list) {
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.list = list;
    }

    public Integer getTotalCount() {
        return this.totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageCount() {
        this.pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public boolean isHasPrevious() {
        this.hasPrevious = !(this.getCurrentPage() != null && this.getCurrentPage() <= 1);
        return this.hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public boolean isHasNext() {
        this.hasNext = this.getPageCount() != null && this.getCurrentPage() != null && this.getPageCount() > this.getCurrentPage();
        return this.hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public List<E> getList() {
        return list;
    }

    public void setList(List<E> list) {
        this.list = list;
    }
}
