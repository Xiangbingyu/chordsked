package com.chordsked.backend.model.entity;

import com.chordsked.backend.model.enums.PermissionResourceType;
import com.chordsked.backend.model.enums.PermissionStatus;

import java.time.LocalDateTime;

public class PermissionEntity {
    private Long id;
    private String code;
    private String name;
    private PermissionResourceType type;
    private Long parentId;
    private String path;
    private Integer sort;
    private PermissionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getType() { return type == null ? null : type.getCode(); }
    public void setType(Integer type) { this.type = PermissionResourceType.fromCode(type); }
    public PermissionResourceType getTypeEnum() { return type; }
    public void setTypeEnum(PermissionResourceType type) { this.type = type; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getStatus() { return status == null ? null : status.getCode(); }
    public void setStatus(Integer status) { this.status = PermissionStatus.fromCode(status); }
    public PermissionStatus getStatusEnum() { return status; }
    public void setStatusEnum(PermissionStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
