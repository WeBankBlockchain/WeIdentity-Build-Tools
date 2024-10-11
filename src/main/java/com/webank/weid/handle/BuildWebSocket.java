

package com.webank.weid.handle;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@ServerEndpoint("/webSocket")
@Component
@Slf4j
public class BuildWebSocket {

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
        log.info("有新连接加入！当前在线人数为" + SocketManager.getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        SocketManager.webSocketSet.remove(this);  // 从set中删除
        SocketManager.subOnlineCount();           // 在线数减1
        log.info("有一连接关闭！当前在线人数为" + SocketManager.getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("来自客户端" + session.getId() + "的消息:" + message);
    }

    /**
     * 发生错误时调用
     * @param session 回话请求
     * @param error 异常堆栈
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error(session.getId() + "发生错误");
        error.printStackTrace();
    }
}
