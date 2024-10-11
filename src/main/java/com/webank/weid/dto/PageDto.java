

package com.webank.weid.dto;

import java.util.List;

import lombok.Data;

@Data
public class PageDto<T> {

    private int startIndex;
    private int pageSize;
    private int allCount;
    private List<T> dataList;
    private T query;

    public PageDto(int startIndex, int pageSize) {
        this.startIndex = startIndex;
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }
}
