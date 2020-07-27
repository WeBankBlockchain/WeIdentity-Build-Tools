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

import org.apache.commons.lang3.StringUtils;

import com.webank.weid.util.DateFormatUtils;

import lombok.Data;

@Data
public class BaseDto implements Comparable<BaseDto>  {
    
    private long time;
    private String id;
    private String createTime;
    private String from;
    private String hash;
    private String hashShow;
    
    public void setTime(long time) {
        this.time = time;
        this.createTime = DateFormatUtils.dateToString(time);
    }
    
    @Override
    public int compareTo(BaseDto o) {
        if (this.time < o.time) {
            return 1;
        } else if (this.time == o.time) {
            return 0;
        }
        return -1;
    }
    
    public String getHideValue(String value, int prefixSize, int suffixSize) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        if (value.length() < (prefixSize + suffixSize)) {
            return value;
        }
        String prefix = value.substring(0, prefixSize);
        String suffix = value.substring(value.length() - suffixSize);
        return prefix + "..." + suffix;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
        this.hashShow =  this.getHideValue(hash, 6, 6);
    }
}
