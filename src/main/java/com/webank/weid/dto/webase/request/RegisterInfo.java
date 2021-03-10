package com.webank.weid.dto.webase.request;

import lombok.Data;

@Data
public class RegisterInfo {

    private String weBaseHost;
    private int weBasePort;
    private String appKey;
    private String appSecret;
    private String weIdHost;
    private int weIdPort;
    private boolean useWebase;
}
