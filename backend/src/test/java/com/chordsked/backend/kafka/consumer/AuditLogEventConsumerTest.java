package com.chordsked.backend.kafka.consumer;

import com.chordsked.backend.kafka.model.AuditLogEvent;
import com.chordsked.backend.service.audit.AuditLogPersistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AuditLogEventConsumerTest {
    private AuditLogPersistService auditLogPersistService;
    private AuditLogEventConsumer auditLogEventConsumer;
    private Acknowledgment acknowledgment;

    @BeforeEach
    void setUp() {
        auditLogPersistService = mock(AuditLogPersistService.class);
        acknowledgment = mock(Acknowledgment.class);
        auditLogEventConsumer = new AuditLogEventConsumer();
        ReflectionTestUtils.setField(auditLogEventConsumer, "auditLogPersistService", auditLogPersistService);
    }

    @Test
    void shouldPersistAndAcknowledgeWhenEventIsValid() {
        AuditLogEvent event = buildValidEvent();

        auditLogEventConsumer.consume(event, acknowledgment);

        verify(auditLogPersistService).persistSync(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldAcknowledgeAndIgnoreWhenEventIsInvalid() {
        AuditLogEvent event = buildValidEvent();
        event.setEventId(" ");

        auditLogEventConsumer.consume(event, acknowledgment);

        verify(auditLogPersistService, never()).persistSync(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldNotAcknowledgeWhenPersistFails() {
        AuditLogEvent event = buildValidEvent();
        RuntimeException persistException = new IllegalStateException("Persist audit log failed");
        doThrow(persistException).when(auditLogPersistService).persistSync(any());

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> auditLogEventConsumer.consume(event, acknowledgment)
        );

        verify(auditLogPersistService).persistSync(any());
        verify(acknowledgment, never()).acknowledge();
        assertSame(persistException, thrown);
    }

    private AuditLogEvent buildValidEvent() {
        AuditLogEvent event = new AuditLogEvent();
        event.setEventId("evt-001");
        event.setModuleName("AUTH");
        event.setActionType("LOGIN");
        event.setStatus(1);
        event.setCreatedAt(System.currentTimeMillis());
        return event;
    }
}
