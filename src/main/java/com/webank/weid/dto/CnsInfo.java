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
