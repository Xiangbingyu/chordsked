package com.chordsked.backend.model.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(name = "RoleDetailQueryRequest", description = "角色详情查询参数")
public class RoleDetailQueryRequest {
    @Schema(description = "角色ID", example = "1")
    @Min(1)
    private Long roleId;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
