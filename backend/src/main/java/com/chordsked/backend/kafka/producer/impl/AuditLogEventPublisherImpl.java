package com.chordsked.backend.kafka.producer.impl;

import com.chordsked.backend.config.properties.KafkaAuditProperties;
import com.chordsked.backend.kafka.model.AuditLogEvent;
import com.chordsked.backend.kafka.producer.AuditLogEventPublisher;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service("auditLogEventPublisher")
public class AuditLogEventPublisherImpl implements AuditLogEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(AuditLogEventPublisherImpl.class);

    @Resource(name = "kafkaAuditProperties")
    private KafkaAuditProperties kafkaAuditProperties;

    @Resource(name = "auditLogKafkaTemplate")
    private KafkaTemplate<String, AuditLogEvent> kafkaTemplate;

    @Override
    public void publish(AuditLogEvent event) {
        if (event == null) {
            return;
        }
        if (event.getEventId() == null || event.getEventId().isBlank()) {
            return;
        }
        kafkaTemplate.send(kafkaAuditProperties.getTopic(), event.getEventId(), event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        logger.error("Publish audit event failed, eventId={}, topic={}",
                                event.getEventId(), kafkaAuditProperties.getTopic(), throwable);
                        return;
                    }
                    if (result == null || result.getRecordMetadata() == null) {
                        logger.warn("Publish audit event result missing metadata, eventId={}, topic={}",
                                event.getEventId(), kafkaAuditProperties.getTopic());
                        return;
                    }
                    logger.debug("Publish audit event success, eventId={}, topic={}, partition={}, offset={}",
                            event.getEventId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                });
    }
}
