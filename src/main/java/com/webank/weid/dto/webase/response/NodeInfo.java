package com.webank.weid.dto.webase.response;

import java.util.ArrayList;

import lombok.Data;

@Data
public class NodeInfo {
    private int frontId;
    private String nodeId; 
    private String frontIp;
    private int frontPort;
    private String agency;
    private String clientVersion;
    private String supportVersion;
    private String frontVersion;
    private String signVersion; 
    private String createTime;
    private String modifyTime;
    private int status;
    private int runType;
    private String agencyId;
    private String agencyName;
    private String hostId;
    private String hostIndex;
    private String imageTag;
    private String containerName;
    private int jsonrpcPort; 
    private int p2pPort;
    private int channelPort;
    private int chainId;
    private String chainName;
    private ArrayList<String> groupList;
}
