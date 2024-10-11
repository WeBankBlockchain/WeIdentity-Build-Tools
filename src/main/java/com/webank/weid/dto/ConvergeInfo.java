

package com.webank.weid.dto;

import java.util.Date;

import lombok.Data;

@Data
public class ConvergeInfo {
    // 汇聚编号
    private String convergeId;
    // 文件路径
    private String filePath;
    //数据来源Ip
    private String formIp;
    // 文件时间
    private String dataTime;
    // 汇聚状态，0初始状态，1失败，2成功
    private int status;
    
    private Date createTime;
    private Date updateTime;
}
