

package com.webank.weid.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AsyncInfo {
    private int id;
    @JsonProperty("data_time")
    private int dataTime; 
    private int status;
    @JsonProperty("all_size")
    private int allSize;
    @JsonProperty("success_size")
    private int successSize; 
    @JsonProperty("fail_size")
    private int failSize; 
    @JsonProperty("create_time")
    private Date createTime; 
    @JsonProperty("update_time")
    private Date updateTime;
    private boolean isLock;
    //数据状态
    private int binLogStatus;
}
