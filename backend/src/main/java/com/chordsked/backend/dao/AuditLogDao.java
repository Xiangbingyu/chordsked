package com.chordsked.backend.dao;

import com.chordsked.backend.model.entity.AuditLogEntity;

public interface AuditLogDao {
    int save(AuditLogEntity auditLog);
}
