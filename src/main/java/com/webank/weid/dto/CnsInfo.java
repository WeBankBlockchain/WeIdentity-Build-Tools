package com.webank.weid.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CnsInfo extends BaseDto {
    
    private String weId;
    private String weIdShow;
    private boolean enable;
    private boolean showDeployCptBtn;
    private boolean needDeployCpt;
    private String issuer;

    public void setWeId(String weId) {
        this.weId = weId;
        this.weIdShow = super.getHideValue(weId, 20, 6);
    }
}
