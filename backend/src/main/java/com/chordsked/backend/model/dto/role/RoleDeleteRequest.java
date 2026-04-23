package com.chordsked.backend.model.dto.role;

import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(name = "RoleDeleteRequest", description = "删除角色请求参数")
public class RoleDeleteRequest {
    @Schema(description = "角色ID", example = "1")
    @Min(1)
    private Long roleId;

    @Schema(hidden = true)
    private AuditLogRecordRequest auditLogRequest;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public AuditLogRecordRequest getAuditLogRequest() {
        return auditLogRequest;
    }

    public void setAuditLogRequest(AuditLogRecordRequest auditLogRequest) {
        this.auditLogRequest = auditLogRequest;
    }
}
