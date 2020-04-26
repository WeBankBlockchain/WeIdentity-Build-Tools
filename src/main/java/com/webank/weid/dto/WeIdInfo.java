package com.webank.weid.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WeIdInfo extends BaseDto {

    private String weId;
    private String ecdsaKey;
    private String ecdsaPubKey;
    private boolean isIssuer;
    private String weIdShow;
    private boolean isAdmin;
    
    public void setWeId(String weId) {
        this.weId = weId;
        this.weIdShow = super.getHideValue(weId, 20, 6);
    }
}
