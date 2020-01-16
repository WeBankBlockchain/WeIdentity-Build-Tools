package com.webank.weid.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeployInfo extends BaseDto {
    
    private String hash;
    private String nodeVerion;
    private String nodeAddress;
    private String authorityAddress;
    private String cptAddress;
    private String evidenceAddress;
    private String specificAddress;
    private String weIdAddress;
    private String ecdsaKey;
    private String ecdsaPublicKey;
    private String contractVersion;
    private String weIdSdkVersion;
    private boolean deploySystemCpt;
    private boolean local;
}
