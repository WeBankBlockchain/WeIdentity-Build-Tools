

package com.webank.weid.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PojoInfo extends BaseDto {
    
    private Integer[] cptIds;
    private String fromType;
    private String showCptIds;
    
    public void setCptIds(Integer[] cptIds) {
        this.cptIds = cptIds;
        StringBuffer s = new StringBuffer();
        for (Integer integer : cptIds) {
            s.append(integer).append(",");
        }
        s.deleteCharAt(s.length() - 1);
        this.showCptIds = super.getHideValue(s.toString(), 20, 0);
    }
}
