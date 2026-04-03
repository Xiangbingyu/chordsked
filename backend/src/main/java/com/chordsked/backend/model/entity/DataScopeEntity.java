package com.chordsked.backend.model.entity;

import com.chordsked.backend.model.enums.DataScopeRangeType;
import com.chordsked.backend.model.enums.DataScopeTargetType;

public class DataScopeEntity {
    private Long id;
    private DataScopeTargetType targetType;
    private Long targetId;
    private DataScopeRangeType scopeType;
    private String campusIds;
    private Long createdAt;
    private Long updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTargetType() {
        return targetType == null ? null : targetType.getCode();
    }

    public void setTargetType(Integer targetType) {
        this.targetType = DataScopeTargetType.fromCode(targetType);
    }

    public DataScopeTargetType getTargetTypeEnum() {
        return targetType;
    }

    public void setTargetTypeEnum(DataScopeTargetType targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Integer getScopeType() {
        return scopeType == null ? null : scopeType.getCode();
    }

    public void setScopeType(Integer scopeType) {
        this.scopeType = DataScopeRangeType.fromCode(scopeType);
    }

    public DataScopeRangeType getScopeTypeEnum() {
        return scopeType;
    }

    public void setScopeTypeEnum(DataScopeRangeType scopeType) {
        this.scopeType = scopeType;
    }

    public String getCampusIds() {
        return campusIds;
    }

    public void setCampusIds(String campusIds) {
        this.campusIds = campusIds;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
