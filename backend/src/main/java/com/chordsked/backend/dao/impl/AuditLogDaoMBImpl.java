package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.AuditLogDao;
import com.chordsked.backend.dao.mapper.AuditLogMapper;
import com.chordsked.backend.model.entity.AuditLogEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository("auditLogDao")
public class AuditLogDaoMBImpl implements AuditLogDao {
    @Resource(name = "auditLogMapper")
    private AuditLogMapper auditLogMapper;

    @Override
    public int save(AuditLogEntity auditLog) {
        if (auditLog == null) {
            return 0;
        }
        if (auditLog.getModuleName() == null || auditLog.getModuleName().isBlank()) {
            return 0;
        }
        if (auditLog.getActionType() == null || auditLog.getActionType().isBlank()) {
            return 0;
        }
        if (auditLog.getStatus() == null) {
            return 0;
        }
        if (auditLog.getCreatedAt() == null || auditLog.getCreatedAt() <= 0) {
            return 0;
        }
        return auditLogMapper.save(auditLog);
    }
}
