package com.chordsked.backend.model.entity;

import com.chordsked.backend.model.enums.StudentUserStatus;

public class StudentUserEntity {
    private Long id;
    private String phone;
    private String name;
    private String avatar;
    private StudentUserStatus status;
    private Long campusId;
    private Long createdAt;
    private Long updatedAt;

    public StudentUserEntity() {
    }

    public StudentUserEntity(
            Long id,
            String phone,
            String name,
            String avatar,
            Integer status,
            Long campusId
    ) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.avatar = avatar;
        this.status = StudentUserStatus.fromCode(status);
        this.campusId = campusId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getStatus() {
        return status == null ? null : status.getCode();
    }

    public void setStatus(Integer status) {
        this.status = StudentUserStatus.fromCode(status);
    }

    public StudentUserStatus getStatusEnum() {
        return status;
    }

    public void setStatusEnum(StudentUserStatus status) {
        this.status = status;
    }

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
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
