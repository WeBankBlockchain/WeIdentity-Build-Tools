

package com.webank.weid.dto;

import com.webank.weid.util.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;

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
