package org.tinycloud.mmwiki.web;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.github.pagehelper.PageInfo;

/**
 * 分页模型类，用于 Ajax 分页查询返回结果。
 *
 * @param <T> 记录类型
 * @author liuxingyu01
 * @since 2026-05-11
 */
public class PageModel<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数。
     */
    private Long totalCount;

    /**
     * 总页数。
     */
    private Long totalPage;

    /**
     * 结果集。
     */
    private List<T> records;

    /**
     * 当前页码。
     */
    private Long pageNo;

    /**
     * 当前页大小。
     */
    private Long pageSize;

    public PageModel() {
    }

    public PageModel(Long pageNo, Long pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public PageModel(Long pageNo, Long pageSize, List<T> records, Long totalCount) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.records = records;
        this.totalCount = totalCount;
        if (totalCount != null && totalCount != 0 && pageSize != null && pageSize != 0) {
            this.totalPage = (totalCount - 1) / pageSize + 1;
        } else {
            this.totalPage = 0L;
        }
    }

    public PageModel(Long pageNo, Long pageSize, List<T> records, Long totalCount, Long totalPage) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.records = records;
        this.totalCount = totalCount;
        this.totalPage = totalPage;
    }

    public static <T> PageModel<T> build(Long page, Long size, List<T> list, Long totalCount, Long totalPage) {
        return new PageModel<>(page, size, list, totalCount, totalPage);
    }

    public static <T> PageModel<T> build(Long page, Long size, List<T> list, Long totalCount) {
        return new PageModel<>(page, size, list, totalCount);
    }

    public static <T> PageModel<T> from(PageInfo<T> pageInfo) {
        return build((long) pageInfo.getPageNum(), (long) pageInfo.getPageSize(), pageInfo.getList(), pageInfo.getTotal(), (long) pageInfo.getPages());
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Long totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public Long getPageNo() {
        return pageNo;
    }

    public void setPageNo(Long pageNo) {
        this.pageNo = pageNo;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}
