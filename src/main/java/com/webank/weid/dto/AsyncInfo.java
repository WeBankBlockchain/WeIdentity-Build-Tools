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
