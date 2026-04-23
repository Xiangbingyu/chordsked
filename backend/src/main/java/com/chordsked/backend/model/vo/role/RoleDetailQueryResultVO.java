package com.chordsked.backend.model.vo.role;

import com.chordsked.backend.model.enums.RoleStatus;
import com.chordsked.backend.model.vo.permission.InternalPermissionTreeQueryResultVO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "RoleDetailQueryResultVO", description = "角色详情")
public class RoleDetailQueryResultVO {
    @Schema(description = "角色ID", example = "1")
    private Long id;

    @Schema(description = "角色编码", example = "ADMIN")
    private String code;

    @Schema(description = "角色名称", example = "超级管理员")
    private String name;

    @Schema(description = "角色描述", example = "系统最高权限角色")
    private String description;

    @Schema(description = "角色状态", example = "1")
    private RoleStatus status;

    @Schema(description = "绑定权限数量", example = "27")
    private Integer permissionCount;

    @Schema(description = "关联用户数量", example = "3")
    private Integer userCount;

    @Schema(description = "角色已分配权限ID列表")
    private List<Long> permissionIds;

    @Schema(description = "完整权限树")
    private List<InternalPermissionTreeQueryResultVO> permissionTree;

    @Schema(description = "创建时间", example = "1774483200000")
    private Long createdAt;

    @Schema(description = "更新时间", example = "1774483200000")
    private Long updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        return status == null ? null : status.getCode();
    }

    public void setStatus(Integer status) {
        this.status = RoleStatus.fromCode(status);
    }

    public RoleStatus getStatusEnum() {
        return status;
    }

    public void setStatusEnum(RoleStatus status) {
        this.status = status;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getPermissionCount() {
        return permissionCount;
    }

    public void setPermissionCount(Integer permissionCount) {
        this.permissionCount = permissionCount;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public List<InternalPermissionTreeQueryResultVO> getPermissionTree() {
        return permissionTree;
    }

    public void setPermissionTree(List<InternalPermissionTreeQueryResultVO> permissionTree) {
        this.permissionTree = permissionTree;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
