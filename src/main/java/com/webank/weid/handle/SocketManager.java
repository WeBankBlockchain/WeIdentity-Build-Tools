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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SocketManager.class);
    
    // 用来记录当前在线连接数
    private static volatile int onlineCount = 0;

    // 标记文件已读行数
    private static volatile long lines = 0L;

    // 判断是否是第一次加载
    private static volatile boolean running = false;

    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象
    // 若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    public static CopyOnWriteArraySet<BuildWebSocket> webSocketSet = new CopyOnWriteArraySet<>();
    
    private static FileAlterationMonitor monitor;
    
    private static String rootDir;
    
    static {
        rootDir = new File("logs").getAbsolutePath();
        System.out.println("rootDir:" + rootDir);
    }

    // 开启监听
    public static synchronized void startListening() {
        File file = new File(rootDir);
        if (!file.exists()) {
            logger.error("[startListening] the monitor path not exists.");
            return;
        }
        
        if (running) {
            return;
        }
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
        monitor = new FileAlterationMonitor(interval, observer);
        // 开始监控
        try {
            monitor.start();
            running = true;
        } catch (Exception e) {
            logger.error("[startListening] the monitor start error.", e);
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        if (onlineCount == 0) {
            startListening();
        }
        onlineCount++;
        
    }

    public static synchronized void subOnlineCount() {
        onlineCount--;
        if (onlineCount == 0) {
            try {
                monitor.stop();
                running = false;
            } catch (Exception e) {
                logger.error("[subOnlineCount] stop the monitor error.", e);
            }
        }
    }

    private static class LogListener extends FileAlterationListenerAdaptor {

        @Override
        public void onFileChange(File file) {
            BufferedReader br;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                br = new BufferedReader(isr);
                if (br.ready()) {
                    lines += br.lines()
                        .skip(lines <= 0L ? 0L : lines)
                        .peek(LogListener::sendMsgToAll)
                        .count();
                }
            } catch (Exception e) {
                logger.error("[onFileChange] read the log file error.", e);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        logger.error("[onFileChange] FileInputStream close error.", e);
                    }
                }
            }
        }

        private static void sendMsgToAll(String msg) {
            webSocketSet.forEach(item -> {
                try {
                    item.getSession().getBasicRemote().sendText(msg);
                } catch (IOException ex) {
                    logger.error("[sendMsgToAll] send message error.", ex);
                }
            });
        }
    }
}

