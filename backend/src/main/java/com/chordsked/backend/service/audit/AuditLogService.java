package com.chordsked.backend.service.audit;

import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;

public interface AuditLogService {
    void record(AuditLogRecordRequest request);
}
