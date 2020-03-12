package com.webank.weid.handle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

public class SocketManager {
    // 用来记录当前在线连接数
    private static volatile int onlineCount = 0;

    // 标记文件已读行数
    private static volatile long lines = 0L;

    // 判断是否是第一次加载
    private static volatile boolean isFirstRunning = true;

    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象
    // 若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    public static CopyOnWriteArraySet<BuildWebSocket> webSocketSet = new CopyOnWriteArraySet<>();

    // 开启监听
    public static synchronized void startListening(String rootDir) {
        if (!isFirstRunning) {
            return;
        }
        System.out.println("rootDir:" + rootDir);
        // 监控目录
        // 轮询间隔 1 秒
        long interval = TimeUnit.SECONDS.toMillis(1);
        // 创建一个文件观察器用于处理文件的格式
        FileAlterationObserver observer = new FileAlterationObserver(
                rootDir,
                FileFilterUtils.and(
                        FileFilterUtils.suffixFileFilter("all.log")),  //过滤文件格式
                null);
        observer.addListener(new LogListener());
        // 创建文件变化监听器
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        // 开始监控
        try {
            monitor.start();
            isFirstRunning = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        onlineCount--;
    }

    private static class LogListener extends FileAlterationListenerAdaptor {

        @Override
        public void onFileChange(File file) {
            BufferedReader br;
            try {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
                br = new BufferedReader(isr);
                if (br.ready()) {
                    lines += br.lines()
                            .skip(lines <= 0L ? 0L : lines)
                            .peek(LogListener::sendMsgToAll)
                            .count();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static void sendMsgToAll(String msg) {
            webSocketSet.forEach(item -> {
                try {
                    item.getSession().getBasicRemote().sendText(msg);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }

    }
}

