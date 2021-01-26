package com.webank.weid.dto;

import lombok.Data;

@Data
public class DataPanel {
    private int blockNumber;
    private int cptCount;
    private int weIdCount;
    private int credentialCount;
    private int issuerCount;
    private int policyCount;
}
