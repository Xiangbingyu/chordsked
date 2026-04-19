package com.chordsked.backend.dao;

import com.chordsked.backend.model.entity.PermissionEntity;

import java.util.List;

public interface PermissionDao {
    PermissionEntity getById(Long id);

    List<PermissionEntity> listByUserType(String userType);

    List<PermissionEntity> listByRoleId(Long roleId);

    List<PermissionEntity> listByInternalUserId(Long userId);

    List<String> listPermissionCodesByInternalUserId(Long userId);

    List<String> listPermissionCodesByUserType(String userType);
}
