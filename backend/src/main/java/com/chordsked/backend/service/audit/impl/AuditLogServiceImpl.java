package com.chordsked.backend.service.audit.impl;

import com.chordsked.backend.config.properties.KafkaAuditProperties;
import com.chordsked.backend.kafka.model.AuditLogEvent;
import com.chordsked.backend.kafka.producer.AuditLogEventPublisher;
import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import com.chordsked.backend.model.entity.AuditLogEntity;
import com.chordsked.backend.model.enums.AuditLogStatus;
import com.chordsked.backend.service.audit.AuditLogPersistService;
import com.chordsked.backend.service.audit.AuditLogService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service("auditLogService")
public class AuditLogServiceImpl implements AuditLogService {
    private static final Logger logger = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    @Resource(name = "auditLogPersistService")
    private AuditLogPersistService auditLogPersistService;

    @Resource(name = "kafkaAuditProperties")
    private KafkaAuditProperties kafkaAuditProperties;

    @Resource(name = "auditLogEventPublisher")
    private AuditLogEventPublisher auditLogEventPublisher;

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
        if (kafkaAuditProperties.isEnabled()) {
            try {
                auditLogEventPublisher.publish(buildAuditLogEvent(auditLog));
                return;
            } catch (RuntimeException exception) {
                logger.error("Publish audit event failed, fallback to async persist", exception);
            }
        }
        auditLogPersistService.persist(auditLog);
    }

    private AuditLogEvent buildAuditLogEvent(AuditLogEntity auditLog) {
        AuditLogEvent event = new AuditLogEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventVersion("v1");
        event.setOccurredAt(System.currentTimeMillis());
        event.setTraceId(MDC.get("traceId"));
        event.setRequestId(MDC.get("requestId"));
        event.setUserId(auditLog.getUserId());
        event.setUserName(auditLog.getUserName());
        event.setUserType(auditLog.getUserType());
        event.setModuleName(auditLog.getModuleName());
        event.setActionType(auditLog.getActionType());
        event.setBizId(auditLog.getBizId());
        event.setRequestUri(auditLog.getRequestUri());
        event.setRequestMethod(auditLog.getRequestMethod());
        event.setRequestIp(auditLog.getRequestIp());
        event.setUserAgent(auditLog.getUserAgent());
        event.setRequestParams(auditLog.getRequestParams());
        event.setResponseResult(auditLog.getResponseResult());
        event.setStatus(auditLog.getStatus());
        event.setErrorMsg(auditLog.getErrorMsg());
        event.setCreatedAt(auditLog.getCreatedAt());
        return event;
    }
}
