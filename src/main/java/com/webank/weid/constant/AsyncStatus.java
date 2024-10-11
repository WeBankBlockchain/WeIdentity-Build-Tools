

package com.webank.weid.constant;

public enum AsyncStatus {
    
    // 初始化状态,此状态没有使用
    INIT(0),
    // 运行状态
    RUNNING(1),
    // 处理成功
    SUCCESS(2),
    // 处理失败
    FAIL(3);
    private int code;
    
    AsyncStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
    
}
