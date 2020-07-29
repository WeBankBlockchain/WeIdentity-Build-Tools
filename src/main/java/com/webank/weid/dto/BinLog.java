/*
 *       Copyright© (2018-2020) WeBank Co., Ltd.
 *
 *       This file is part of weidentity-build-tools.
 *
 *       weidentity-build-tools is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Lesser General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       weidentity-build-tools is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Lesser General Public License for more details.
 *
 *       You should have received a copy of the GNU Lesser General Public License
 *       along with weidentity-build-tools.  If not, see <https://www.gnu.org/licenses/>.
 */

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
