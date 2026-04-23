package com.chordsked.backend.model.entity;

import com.chordsked.backend.model.enums.InternalUserStatus;
import com.chordsked.backend.model.enums.MustChangePasswordFlag;
import com.chordsked.backend.model.enums.UserDataScopeType;

public class InternalUserEntity {
    private Long id;
    private String username;
    private String password;
    private String phone;
    private String name;
    private String avatar;
    private InternalUserStatus status;
    private MustChangePasswordFlag mustChangePassword;
    private UserDataScopeType dataScopeType;
    private Long createdAt;
    private Long updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
        this.status = InternalUserStatus.fromCode(status);
    }

    public InternalUserStatus getStatusEnum() {
        return status;
    }

    public void setStatusEnum(InternalUserStatus status) {
        this.status = status;
    }

    public Integer getMustChangePassword() {
        return mustChangePassword == null ? null : mustChangePassword.getCode();
    }

    public void setMustChangePassword(Integer mustChangePassword) {
        this.mustChangePassword = MustChangePasswordFlag.fromCode(mustChangePassword);
    }

    public MustChangePasswordFlag getMustChangePasswordEnum() {
        return mustChangePassword;
    }

    public void setMustChangePasswordEnum(MustChangePasswordFlag mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public Integer getDataScopeType() {
        return dataScopeType == null ? null : dataScopeType.getCode();
    }

    public void setDataScopeType(Integer dataScopeType) {
        this.dataScopeType = UserDataScopeType.fromCode(dataScopeType);
    }

    public UserDataScopeType getDataScopeTypeEnum() {
        return dataScopeType;
    }

    public void setDataScopeTypeEnum(UserDataScopeType dataScopeType) {
        this.dataScopeType = dataScopeType;
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
