package com.chordsked.backend.model.entity;

import com.chordsked.backend.model.enums.DataScopeRangeType;
import com.chordsked.backend.model.enums.DataScopeTargetType;

import java.time.LocalDateTime;

public class DataScopeEntity {
    private Long id;
    private DataScopeTargetType targetType;
    private Long targetId;
    private DataScopeRangeType scopeType;
    private String campusIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTargetType() { return targetType == null ? null : targetType.name(); }
    public void setTargetType(String targetType) { this.targetType = DataScopeTargetType.fromValue(targetType); }
    public DataScopeTargetType getTargetTypeEnum() { return targetType; }
    public void setTargetTypeEnum(DataScopeTargetType targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getScopeType() { return scopeType == null ? null : scopeType.name(); }
    public void setScopeType(String scopeType) { this.scopeType = DataScopeRangeType.fromValue(scopeType); }
    public DataScopeRangeType getScopeTypeEnum() { return scopeType; }
    public void setScopeTypeEnum(DataScopeRangeType scopeType) { this.scopeType = scopeType; }
    public String getCampusIds() { return campusIds; }
    public void setCampusIds(String campusIds) { this.campusIds = campusIds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
