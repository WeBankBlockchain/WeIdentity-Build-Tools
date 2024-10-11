

package com.webank.weid.dto;

import lombok.Data;

@Data
public class ConvergeResult {
    private int successSize;
    private int failSize;
    private int allSize;
    private int result;// 0汇聚成功，1汇聚失败，2已汇聚
    private String fileName;
    private String dataTime;
    
    public ConvergeResult(String dataTime, String fileName) {
        this.dataTime = dataTime;
        this.fileName = fileName;
    }
}
