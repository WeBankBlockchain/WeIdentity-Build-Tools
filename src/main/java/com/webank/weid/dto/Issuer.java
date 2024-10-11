

package com.webank.weid.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Issuer extends BaseDto {

    private String weId;
    private String name;
    private boolean canDo;
    private String weIdShow;
    private boolean recognized;
    private String description;
    public void setWeId(String weId) {
        this.weId = weId;
        this.weIdShow = super.getHideValue(weId, 20, 6);
    }
}
