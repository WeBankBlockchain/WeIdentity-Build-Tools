

package com.webank.weid.dto;

import com.webank.weid.protocol.base.AuthorityIssuer;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShareInfo extends DeployBase {

    private String owner;
    private String ownerShow;
    private String groupId;
    private String showGroupId;
    private String evidenceAddress;
    private boolean enable; 
    private AuthorityIssuer issuer;
    private boolean showBtn;
    private String evidenceName;
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
        this.showGroupId = "group-" + groupId;
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
        this.ownerShow = super.getHideValue(owner, 20, 6);
    }
    
    @Override
    public int compareTo(BaseDto o) {
        if (o != null) {
            ShareInfo c = (ShareInfo) o;
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
