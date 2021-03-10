package com.webank.weid.dto;

import lombok.Data;

@Data
public class ContractSolInfo {

    private String contractName;
    private String contractSource;
    private String contractAbi;
    private String bytecodeBin;
}
