package org.tinycloud.mmwiki.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 线程池配置类。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolConfig.class);

    @Value("${async.executor.thread.core-pool-size}")
    private int corePoolSize;

    @Value("${async.executor.thread.max-pool-size}")
    private int maxPoolSize;

    @Value("${async.executor.thread.keep-alive-seconds}")
    private int keepAliveSeconds;

    @Value("${async.executor.thread.queue-capacity}")
    private int queueCapacity;

    @Value("${async.executor.thread.name-prefix}")
    private String namePrefix;

    /**
     * 应用通用异步线程池。
     */
    @Primary
    @Bean("asyncServiceExecutor")
    public ThreadPoolTaskExecutor asyncServiceExecutor() {
        log.info("start initializing asyncServiceExecutor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(namePrefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        log.info("finish initializing asyncServiceExecutor");
        return executor;
    }
}
