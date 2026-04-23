package com.chordsked.backend.service.audit.impl;

import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import com.chordsked.backend.model.entity.AuditLogEntity;
import com.chordsked.backend.model.enums.AuditLogStatus;
import com.chordsked.backend.service.audit.AuditLogPersistService;
import com.chordsked.backend.service.audit.AuditLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service("auditLogService")
public class AuditLogServiceImpl implements AuditLogService {
    @Resource(name = "auditLogPersistService")
    private AuditLogPersistService auditLogPersistService;

    @Override
    public void record(AuditLogRecordRequest request) {
        if (!isValidRecordRequest(request)) {
            return;
        }
        AuditLogEntity auditLog = buildAuditLogEntity(request);
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            if (AuditLogStatus.FAILED.getCode() == request.getStatus()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) {
                            dispatchPersist(auditLog);
                        }
                    }
                });
                return;
            }
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    dispatchPersist(auditLog);
                }
            });
            return;
        }
        dispatchPersist(auditLog);
    }

    private boolean isValidRecordRequest(AuditLogRecordRequest request) {
        if (request == null) {
            return false;
        }
        if (request.getModuleName() == null || request.getModuleName().isBlank()) {
            return false;
        }
        if (request.getActionType() == null || request.getActionType().isBlank()) {
            return false;
        }
        return AuditLogStatus.fromCode(request.getStatus()) != null;
    }

    private AuditLogEntity buildAuditLogEntity(AuditLogRecordRequest request) {
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setModuleName(request.getModuleName().trim());
        auditLog.setActionType(request.getActionType().trim());
        auditLog.setBizId(request.getBizId());
        auditLog.setUserId(request.getUserId());
        auditLog.setUserName(request.getUserName());
        auditLog.setUserType(request.getUserType());
        auditLog.setRequestUri(request.getRequestUri());
        auditLog.setRequestMethod(request.getRequestMethod());
        auditLog.setRequestIp(request.getRequestIp());
        auditLog.setUserAgent(request.getUserAgent());
        auditLog.setRequestParams(request.getRequestParams());
        auditLog.setResponseResult(request.getResponseResult());
        auditLog.setStatus(request.getStatus());
        auditLog.setErrorMsg(request.getErrorMsg());
        auditLog.setCreatedAt(System.currentTimeMillis());
        return auditLog;
    }

    private void dispatchPersist(AuditLogEntity auditLog) {
        auditLogPersistService.persist(auditLog);
    }
}
