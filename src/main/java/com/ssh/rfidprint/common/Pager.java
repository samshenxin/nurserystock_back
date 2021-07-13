package com.ssh.rfidprint.common;
import java.util.List;
/**
 * Bean�?- 分页
 */
public class Pager {
    // 排序方式
    public enum OrderType {
        asc, desc
    }
    public static final Integer MAX_PAGE_SIZE = 500;           // 每页�?��记录数限�?
    private Integer             pageNumber    = 1;             // 当前页码
    private Integer             pageSize      = 20;            // 每页记录�?
    private Integer             totalCount    = 0;             // 总记录数
    private Integer             pageCount     = 0;             // 总页�?
    private String              property;                      // 查找属�?名称
    private String              keyword;                       // 查找关键�?
    private String              orderBy       = "id";          // 排序字段
    private OrderType           orderType     = OrderType.desc; // 排序方式
    private List<?>             resultList;                          // 数据List

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        if (pageSize < 1) {
            pageSize = 1;
        } else if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        this.pageSize = pageSize;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getPageCount() {
        pageCount = totalCount / pageSize;
        if (totalCount % pageSize > 0) {
            pageCount++;
        }
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public List<?> getResultList() {
        return resultList;
    }

    public void setResultList(List<?> resultList) {
        this.resultList = resultList;
    }

}