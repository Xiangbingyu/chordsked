package com.chordsked.backend.model.dto.org;

import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(name = "OrgNodeUserBindUpdateRequest", description = "组织节点绑定账号更新请求参数")
public class OrgNodeUserBindUpdateRequest {
    @Schema(description = "账号ID列表")
    private List<@NotNull @Min(1) Long> userIds;

    @Schema(hidden = true)
    private AuditLogRecordRequest auditLogRequest;

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public AuditLogRecordRequest getAuditLogRequest() {
        return auditLogRequest;
    }

    public void setAuditLogRequest(AuditLogRecordRequest auditLogRequest) {
        this.auditLogRequest = auditLogRequest;
    }
}
