package com.chordsked.backend.service.audit.impl;

import com.chordsked.backend.dao.AuditLogDao;
import com.chordsked.backend.model.entity.AuditLogEntity;
import com.chordsked.backend.service.audit.AuditLogPersistService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service("auditLogPersistService")
public class AuditLogPersistServiceImpl implements AuditLogPersistService {
    private static final Logger logger = LoggerFactory.getLogger(AuditLogPersistServiceImpl.class);

    @Resource(name = "auditLogDao")
    private AuditLogDao auditLogDao;

    @Override
    public void persistSync(AuditLogEntity auditLog) {
        if (auditLog == null) {
            return;
        }
        int affectedRows = auditLogDao.save(auditLog);
        if (affectedRows <= 0) {
            throw new IllegalStateException("Persist audit log failed");
        }
    }

    @Override
    @Async("auditLogExecutor")
    public void persist(AuditLogEntity auditLog) {
        try {
            persistSync(auditLog);
        } catch (RuntimeException exception) {
            logger.error(
                    "Audit log save failed, moduleName={}, actionType={}, bizId={}",
                    auditLog.getModuleName(),
                    auditLog.getActionType(),
                    auditLog.getBizId(),
                    exception
            );
        }
    }
}
