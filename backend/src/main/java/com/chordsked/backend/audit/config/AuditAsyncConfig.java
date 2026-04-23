package com.chordsked.backend.audit.config;

import com.chordsked.backend.config.properties.AuditAsyncProperties;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AuditAsyncConfig {
    @Resource(name = "auditAsyncProperties")
    private AuditAsyncProperties auditAsyncProperties;

    @Bean("auditLogExecutor")
    public Executor auditLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(auditAsyncProperties.getCorePoolSize());
        executor.setMaxPoolSize(auditAsyncProperties.getMaxPoolSize());
        executor.setQueueCapacity(auditAsyncProperties.getQueueCapacity());
        executor.setKeepAliveSeconds(auditAsyncProperties.getKeepAliveSeconds());
        executor.setThreadNamePrefix(auditAsyncProperties.getThreadNamePrefix());
        executor.initialize();
        return executor;
    }
}
