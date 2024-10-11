package com.webank.weid.dto.webase.response;

import lombok.Data;

@Data
public class UserInfo {
    private int userId;
    private String userName; 
    private String account;
    private String groupId;
    private String publicKey;
    private String privateKey;
    private int userStatus; 
    private int chainIndex;
    private int userType;
    private String address;
    private String signUserId;
    private int appId;
    private int hasPk;
    private String description;
    private String createTime;
    private String modifyTime;
}
