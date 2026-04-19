package com.chordsked.backend.model.entity;

import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.PermissionStatus;
import com.chordsked.backend.model.enums.PermissionType;

public class PermissionEntity {
    private Long id;
    private String code;
    private String name;
    private PermissionType type;
    private Long parentId;
    private String path;
    private Integer sort;
    private PermissionStatus status;
    private AccountUserType userType;
    private Long createdAt;
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

    public Integer getType() {
        return type == null ? null : type.getCode();
    }

    public void setType(Integer type) {
        this.type = PermissionType.fromCode(type);
    }

    public PermissionType getTypeEnum() {
        return type;
    }

    public void setTypeEnum(PermissionType type) {
        this.type = type;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status == null ? null : status.getCode();
    }

    public void setStatus(Integer status) {
        this.status = PermissionStatus.fromCode(status);
    }

    public PermissionStatus getStatusEnum() {
        return status;
    }

    public void setStatusEnum(PermissionStatus status) {
        this.status = status;
    }

    public String getUserType() {
        return userType == null ? null : userType.getCode();
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : AccountUserType.fromValue(userType);
    }

    public AccountUserType getUserTypeEnum() {
        return userType;
    }

    public void setUserTypeEnum(AccountUserType userType) {
        this.userType = userType;
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
