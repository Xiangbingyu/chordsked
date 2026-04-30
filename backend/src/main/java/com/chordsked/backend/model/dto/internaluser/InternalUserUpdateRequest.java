package com.chordsked.backend.model.dto.internaluser;

import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(name = "InternalUserUpdateRequest", description = "编辑教务端账号请求参数")
public class InternalUserUpdateRequest {
    @Schema(hidden = true)
    @Min(1)
    private Long userId;

    @Schema(description = "手机号", example = "13800138000")
    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "姓名", example = "张三")
    @NotBlank
    @Size(max = 50)
    private String name;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    @Size(max = 500)
    private String avatar;

    @Schema(description = "角色ID列表")
    @NotEmpty
    private List<@NotNull @Min(1) Long> roleIds;

    @Schema(description = "主归属组织节点ID", example = "1")
    @NotNull
    @Min(1)
    private Long primaryOrgNodeId;

    @Schema(description = "组织授权节点ID列表")
    private List<@NotNull @Min(1) Long> orgScopeNodeIds;

    @Schema(description = "数据范围类型", example = "1")
    @NotNull
    private Integer dataScopeType;

    @Schema(hidden = true)
    private AuditLogRecordRequest auditLogRequest;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public Long getPrimaryOrgNodeId() {
        return primaryOrgNodeId;
    }

    public void setPrimaryOrgNodeId(Long primaryOrgNodeId) {
        this.primaryOrgNodeId = primaryOrgNodeId;
    }

    public List<Long> getOrgScopeNodeIds() {
        return orgScopeNodeIds;
    }

    public void setOrgScopeNodeIds(List<Long> orgScopeNodeIds) {
        this.orgScopeNodeIds = orgScopeNodeIds;
    }

    public Integer getDataScopeType() {
        return dataScopeType;
    }

    public void setDataScopeType(Integer dataScopeType) {
        this.dataScopeType = dataScopeType;
    }

    public AuditLogRecordRequest getAuditLogRequest() {
        return auditLogRequest;
    }

    public void setAuditLogRequest(AuditLogRecordRequest auditLogRequest) {
        this.auditLogRequest = auditLogRequest;
    }
}
