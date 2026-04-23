package com.chordsked.backend.service.audit;

import com.chordsked.backend.model.entity.AuditLogEntity;

/**
 * 审计日志落库服务，负责执行最终持久化动作。
 */
public interface AuditLogPersistService {
    void persist(AuditLogEntity auditLog);
}
