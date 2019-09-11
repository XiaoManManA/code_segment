package com.hanyuan.ebay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class EbayAsyncConfigurer {

    @Bean("ebayTaskExecutor")
    public Executor ebayTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(18);            //核心线程数
        executor.setMaxPoolSize(18);             //最大线程数
        executor.setQueueCapacity(5000);        //队列大小
        executor.setKeepAliveSeconds(60 * 5);    //线程最大空闲时间
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("hy-ebay-"); //指定用于新创建的线程名称的前缀。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 拒绝策略
        executor.initialize();
        return executor;
    }

}
