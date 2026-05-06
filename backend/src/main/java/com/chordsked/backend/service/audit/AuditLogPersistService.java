package com.chordsked.backend.service.audit;

import com.chordsked.backend.model.entity.AuditLogEntity;

/**
 * 审计日志落库服务，负责执行最终持久化动作。
 */
public interface AuditLogPersistService {
    /**
     * 同步落库，供 Kafka 消费链路使用，失败时抛出异常以便上层重试或进入 DLT。
     */
    void persistSync(AuditLogEntity auditLog);

    /**
     * 异步兜底落库，供主业务链路在 Kafka 不可用时降级使用。
     */
    void persist(AuditLogEntity auditLog);
}
