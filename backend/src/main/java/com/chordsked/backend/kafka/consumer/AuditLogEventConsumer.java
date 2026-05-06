package com.chordsked.backend.kafka.consumer;

import com.chordsked.backend.kafka.model.AuditLogEvent;
import com.chordsked.backend.model.entity.AuditLogEntity;
import com.chordsked.backend.service.audit.AuditLogPersistService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service("auditLogEventConsumer")
@ConditionalOnProperty(prefix = "chordsked.kafka.audit", name = "enabled", havingValue = "true")
public class AuditLogEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(AuditLogEventConsumer.class);

    @Resource(name = "auditLogPersistService")
    private AuditLogPersistService auditLogPersistService;

    @KafkaListener(
            topics = "${chordsked.kafka.audit.topic}",
            groupId = "${chordsked.kafka.audit.group-id}",
            containerFactory = "auditLogKafkaListenerContainerFactory"
    )
    public void consume(AuditLogEvent event, Acknowledgment acknowledgment) {
        if (!isValidEvent(event)) {
            acknowledgment.acknowledge();
            return;
        }
        auditLogPersistService.persistSync(buildAuditLogEntity(event));
        acknowledgment.acknowledge();
    }

    private boolean isValidEvent(AuditLogEvent event) {
        if (event == null) {
            return false;
        }
        if (event.getEventId() == null || event.getEventId().isBlank()) {
            logger.warn("Ignore audit event due to blank eventId");
            return false;
        }
        if (event.getModuleName() == null || event.getModuleName().isBlank()) {
            logger.warn("Ignore audit event due to blank moduleName, eventId={}", event.getEventId());
            return false;
        }
        if (event.getActionType() == null || event.getActionType().isBlank()) {
            logger.warn("Ignore audit event due to blank actionType, eventId={}", event.getEventId());
            return false;
        }
        return event.getStatus() != null && event.getCreatedAt() != null && event.getCreatedAt() > 0;
    }

    private AuditLogEntity buildAuditLogEntity(AuditLogEvent event) {
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setUserId(event.getUserId());
        auditLog.setUserName(event.getUserName());
        auditLog.setUserType(event.getUserType());
        auditLog.setModuleName(event.getModuleName());
        auditLog.setActionType(event.getActionType());
        auditLog.setBizId(event.getBizId());
        auditLog.setRequestUri(event.getRequestUri());
        auditLog.setRequestMethod(event.getRequestMethod());
        auditLog.setRequestIp(event.getRequestIp());
        auditLog.setUserAgent(event.getUserAgent());
        auditLog.setRequestParams(event.getRequestParams());
        auditLog.setResponseResult(event.getResponseResult());
        auditLog.setStatus(event.getStatus());
        auditLog.setErrorMsg(event.getErrorMsg());
        auditLog.setCreatedAt(event.getCreatedAt());
        return auditLog;
    }
}
