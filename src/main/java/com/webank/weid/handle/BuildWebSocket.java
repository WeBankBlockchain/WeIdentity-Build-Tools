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

package com.webank.weid.handle;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@ServerEndpoint("/webSocket")
@Component
public class BuildWebSocket {
    
    private static final Logger logger = LoggerFactory.getLogger(BuildWebSocket.class);
    
   // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    public Session getSession() {
        return session;
    }

    /**
     * 连接建立成功调用的方法
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        SocketManager.webSocketSet.add(this);     // 加入set中
        SocketManager.addOnlineCount();           // 在线数加1
        logger.info("有新连接加入！当前在线人数为" + SocketManager.getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        SocketManager.webSocketSet.remove(this);  // 从set中删除
        SocketManager.subOnlineCount();           // 在线数减1
        logger.info("有一连接关闭！当前在线人数为" + SocketManager.getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("来自客户端" + session.getId() + "的消息:" + message);
    }

    /**
     * 发生错误时调用
     * @param session 回话请求
     * @param error 异常堆栈
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error(session.getId() + "发生错误");
        error.printStackTrace();
    }
}
