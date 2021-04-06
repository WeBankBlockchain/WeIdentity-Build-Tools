package com.webank.weid.dto.webase.request;

import lombok.Data;

@Data
public class RegisterInfo {

    private String weBaseHost;
    private String appKey;
    private String appSecret;
    private String weIdHost;
    private boolean useWebase;
}
