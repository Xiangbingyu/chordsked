package com.chordsked.backend.model.entity;

import java.time.LocalDateTime;

public class UserCampusEntity {
    private Long id;
    private Long userId;
    private Long campusId;
    private Integer isPrimary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }
    public Integer getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Integer isPrimary) { this.isPrimary = isPrimary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
