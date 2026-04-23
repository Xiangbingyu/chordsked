package com.chordsked.backend.model.dto.internaluser;

import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(name = "InternalUserResetPasswordRequest", description = "教务端账号重置密码请求")
public class InternalUserResetPasswordRequest {
    @Schema(hidden = true)
    @Min(1)
    private Long userId;

    @Schema(description = "重置原因", example = "账号异常，管理员人工重置")
    @Size(max = 200)
    private String reason;

    @Schema(hidden = true)
    private AuditLogRecordRequest auditLogRequest;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AuditLogRecordRequest getAuditLogRequest() {
        return auditLogRequest;
    }

    public void setAuditLogRequest(AuditLogRecordRequest auditLogRequest) {
        this.auditLogRequest = auditLogRequest;
    }
}
