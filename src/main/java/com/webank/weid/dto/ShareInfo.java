package com.webank.weid.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShareInfo extends DeployBase {

    private String owner;
    private String ownerShow;
    private Integer groupId;
    private String showGroupId;
    private String evidenceAddress;
    private boolean enable; 
    private String issuer;
    
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
        this.showGroupId = "group-" + groupId;
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
        this.ownerShow = super.getHideValue(owner, 20, 6);
    }
}
