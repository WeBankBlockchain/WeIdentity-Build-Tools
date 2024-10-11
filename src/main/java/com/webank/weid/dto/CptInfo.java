

package com.webank.weid.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CptInfo extends BaseDto {
    
    private String weId;
    private Integer cptId;
    private String cptTitle;
    private String cptDesc;
    private String cptJsonName;
    private String weIdShow;
    private String cptType;
    public void setWeId(String weId) {
        this.weId = weId;
        this.weIdShow = super.getHideValue(weId, 20, 6);
    }
}
