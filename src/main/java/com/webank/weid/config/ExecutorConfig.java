

package com.webank.weid.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.Data;

@Configuration
@EnableAsync
@ConfigurationProperties(prefix = "async.thread")
@Data
public class ExecutorConfig {

    private int corePoolSize;
    private int maxPoolSize;
    private int maxQueue;
    private String namePrefix;
    private int keepAlive;
    
    @Bean
    public Executor asyncServiceExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数
        executor.setCorePoolSize(corePoolSize);
        //最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        //队列大小
        executor.setQueueCapacity(maxQueue);
        //线程池中的线程的名称前缀
        executor.setThreadNamePrefix(namePrefix);
        //回收时间
        executor.setKeepAliveSeconds(keepAlive);
        // 当pool已经达到max size的时候
        // 不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //执行初始化
        executor.initialize();
        return executor;
    }

}
