package com.webank.weid.dto;

import java.util.List;

import lombok.Data;

@Data
public class PageDto<T> {

    private int startIndex;
    private int pageSize;
    private int allCount;
    private List<T> dataList;
    
    public PageDto(int startIndex, int pageSize) {
        this.startIndex = startIndex;
        this.pageSize = pageSize;
    }
    
    public int getEndIndex() {
        return startIndex + pageSize;
    }

    
}
