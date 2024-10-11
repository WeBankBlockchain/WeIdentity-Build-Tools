

package com.webank.weid.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeployInfo extends DeployBase {
    private String authorityAddress;
    private String cptAddress;
    private String evidenceAddress;
    private String specificAddress;
    private String weIdAddress;
    private boolean deploySystemCpt;
    private String chainId;
}
