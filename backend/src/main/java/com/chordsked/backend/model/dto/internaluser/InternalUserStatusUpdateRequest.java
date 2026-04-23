package com.chordsked.backend.model.dto.internaluser;

import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "InternalUserStatusUpdateRequest", description = "教务端账号状态更新请求")
public class InternalUserStatusUpdateRequest {
    @Schema(hidden = true)
    @Min(1)
    private Long userId;

    @Schema(description = "账号状态，0=禁用，1=启用", example = "0")
    @NotNull
    private Integer status;

    @Schema(hidden = true)
    private AuditLogRecordRequest auditLogRequest;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public AuditLogRecordRequest getAuditLogRequest() {
        return auditLogRequest;
    }

    public void setAuditLogRequest(AuditLogRecordRequest auditLogRequest) {
        this.auditLogRequest = auditLogRequest;
    }
}
