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

import com.webank.weid.protocol.base.AuthorityIssuer;

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
    private AuthorityIssuer issuer;
    private boolean showBtn;
    
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
        this.showGroupId = "group-" + groupId;
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
        this.ownerShow = super.getHideValue(owner, 20, 6);
    }
}
