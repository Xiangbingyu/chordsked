package com.chordsked.backend.dao;

import com.chordsked.backend.model.entity.RolePermissionEntity;

import java.util.List;

public interface RolePermissionDao {
    RolePermissionEntity getById(Long id);

    List<RolePermissionEntity> listByRoleId(Long roleId);

    List<RolePermissionEntity> listByPermissionId(Long permissionId);

    int deleteByRoleId(Long roleId);

    int saveBatch(List<RolePermissionEntity> rolePermissions);
}
