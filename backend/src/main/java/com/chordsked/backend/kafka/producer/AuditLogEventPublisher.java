package com.chordsked.backend.kafka.producer;

import com.chordsked.backend.kafka.model.AuditLogEvent;

public interface AuditLogEventPublisher {
    void publish(AuditLogEvent event);
}
