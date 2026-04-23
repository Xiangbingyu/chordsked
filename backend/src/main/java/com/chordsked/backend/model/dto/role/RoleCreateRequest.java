package com.chordsked.backend.model.dto.role;

import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(name = "RoleCreateRequest", description = "创建角色请求参数")
public class RoleCreateRequest {
    @Schema(description = "角色编码", example = "CAMPUS_ADMIN")
    @NotBlank
    @Size(max = 50)
    private String code;

    @Schema(description = "角色名称", example = "校区管理员")
    @NotBlank
    @Size(max = 50)
    private String name;

    @Schema(description = "角色描述", example = "负责校区日常管理")
    @Size(max = 200)
    private String description;

    @Schema(description = "角色状态", example = "1")
    @NotNull
    private Integer status;

    @Schema(description = "角色权限ID列表")
    @NotEmpty
    private List<@NotNull @Min(1) Long> permissionIds;

    @Schema(hidden = true)
    private AuditLogRecordRequest auditLogRequest;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public AuditLogRecordRequest getAuditLogRequest() {
        return auditLogRequest;
    }

    public void setAuditLogRequest(AuditLogRecordRequest auditLogRequest) {
        this.auditLogRequest = auditLogRequest;
    }
}
