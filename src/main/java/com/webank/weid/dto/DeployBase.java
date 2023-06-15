

package com.webank.weid.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeployBase extends BaseDto {
    private String nodeVerion;
    private String nodeAddress;
    private String privateKey;
    private String publicKey;
    private String contractVersion;
    private String weIdSdkVersion;
    private boolean local;
}
