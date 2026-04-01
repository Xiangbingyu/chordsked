package com.chordsked.backend.model.entity;

import com.chordsked.backend.model.enums.MustChangePasswordFlag;
import com.chordsked.backend.model.enums.TeacherUserLevelType;
import com.chordsked.backend.model.enums.TeacherUserTeachingType;
import com.chordsked.backend.model.enums.TeacherUserStatus;

import java.time.LocalDateTime;

public class TeacherUserEntity {
    private Long id;
    private String teacherNo;
    private String name;
    private String phone;
    private String password;
    private String avatar;
    private TeacherUserTeachingType teachingType;
    private TeacherUserLevelType teacherLevel;
    private Long campusId;
    private TeacherUserStatus status;
    private MustChangePasswordFlag mustChangePassword;
    private Integer loginFailCount;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;
    private String experience;
    private String goodAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTeacherNo() { return teacherNo; }
    public void setTeacherNo(String teacherNo) { this.teacherNo = teacherNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getTeachingType() { return teachingType == null ? null : teachingType.name(); }
    public void setTeachingType(String teachingType) { this.teachingType = TeacherUserTeachingType.fromValue(teachingType); }
    public TeacherUserTeachingType getTeachingTypeEnum() { return teachingType; }
    public void setTeachingTypeEnum(TeacherUserTeachingType teachingType) { this.teachingType = teachingType; }
    public String getTeacherLevel() { return teacherLevel == null ? null : teacherLevel.name(); }
    public void setTeacherLevel(String teacherLevel) { this.teacherLevel = TeacherUserLevelType.fromValue(teacherLevel); }
    public TeacherUserLevelType getTeacherLevelEnum() { return teacherLevel; }
    public void setTeacherLevelEnum(TeacherUserLevelType teacherLevel) { this.teacherLevel = teacherLevel; }
    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }
    public Integer getStatus() { return status == null ? null : status.getCode(); }
    public void setStatus(Integer status) { this.status = TeacherUserStatus.fromCode(status); }
    public TeacherUserStatus getStatusEnum() { return status; }
    public void setStatusEnum(TeacherUserStatus status) { this.status = status; }
    public Integer getMustChangePassword() { return mustChangePassword == null ? null : mustChangePassword.getCode(); }
    public void setMustChangePassword(Integer mustChangePassword) { this.mustChangePassword = MustChangePasswordFlag.fromCode(mustChangePassword); }
    public MustChangePasswordFlag getMustChangePasswordEnum() { return mustChangePassword; }
    public void setMustChangePasswordEnum(MustChangePasswordFlag mustChangePassword) { this.mustChangePassword = mustChangePassword; }
    public Integer getLoginFailCount() { return loginFailCount; }
    public void setLoginFailCount(Integer loginFailCount) { this.loginFailCount = loginFailCount; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getGoodAt() { return goodAt; }
    public void setGoodAt(String goodAt) { this.goodAt = goodAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
