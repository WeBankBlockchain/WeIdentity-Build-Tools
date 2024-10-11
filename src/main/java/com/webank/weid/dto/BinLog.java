

package com.webank.weid.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BinLog extends BaseDto {
    
    private String id;
    // 交易请求Id
    @JsonProperty("request_id")
    private String requestId;
    // 交易方法
    @JsonProperty("transaction_method")
    private String transactionMethod;
    // 交易参数
    @JsonProperty("transaction_args")
    private String transactionArgs;
    // 扩展 
    private String extra;
    // 交易时间
    @JsonProperty("transaction_timestamp")
    private long transactionTimestamp;
    // 文件时间
    private int batch;
    // 交易状态
    private int status;
    // 创建时间
    private Date created;
    // 更新时间
    private Date updated;
}
