package com.chordsked.backend.model.entity;

import com.chordsked.backend.model.enums.RoleStatus;

import java.time.LocalDateTime;

public class RoleEntity {
    private Long id;
    private String code;
    private String name;
    private String description;
    private RoleStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status == null ? null : status.getCode(); }
    public void setStatus(Integer status) { this.status = RoleStatus.fromCode(status); }
    public RoleStatus getStatusEnum() { return status; }
    public void setStatusEnum(RoleStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
