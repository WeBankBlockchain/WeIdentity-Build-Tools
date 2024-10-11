

package com.webank.weid.dto;

import com.webank.weid.protocol.base.AuthorityIssuer;

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
    private AuthorityIssuer issuer;
    private String groupId;
    private String roleType;
    private String applyName;

    public void setWeId(String weId) {
        this.weId = weId;
        this.weIdShow = super.getHideValue(weId, 20, 6);
    }
    
    @Override
    public int compareTo(BaseDto o) {
        if (o != null) {
            CnsInfo c = (CnsInfo) o;
            if (this.enable ^ c.enable) {
                if (this.enable) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
              if (super.getTime() < c.getTime()) {
                  return 1;
              } else if (super.getTime() == c.getTime()) {
                  return 0;
              } else {
                  return -1;
              }
            }
        }
        return -1;
    }
}
