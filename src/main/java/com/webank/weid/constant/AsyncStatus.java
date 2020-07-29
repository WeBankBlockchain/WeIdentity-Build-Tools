/*
 *       Copyright© (2018-2020) WeBank Co., Ltd.
 *
 *       This file is part of weidentity-build-tools.
 *
 *       weidentity-build-tools is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Lesser General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       weidentity-build-tools is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Lesser General Public License for more details.
 *
 *       You should have received a copy of the GNU Lesser General Public License
 *       along with weidentity-build-tools.  If not, see <https://www.gnu.org/licenses/>.
 */

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
