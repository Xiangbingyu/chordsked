package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.PermissionDao;
import com.chordsked.backend.dao.mapper.PermissionMapper;
import com.chordsked.backend.model.entity.PermissionEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("permissionDao")
public class PermissionDaoMBImpl implements PermissionDao {
    @Resource(name = "permissionMapper")
    private PermissionMapper permissionMapper;

    @Override
    public PermissionEntity getById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return permissionMapper.getById(id);
    }

    @Override
    public List<PermissionEntity> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return permissionMapper.listByIds(ids);
    }

    @Override
    public List<PermissionEntity> listByUserType(String userType) {
        if (userType == null || userType.isBlank()) {
            return List.of();
        }
        return permissionMapper.listByUserType(userType.trim());
    }

    @Override
    public List<PermissionEntity> listByRoleId(Long roleId) {
        if (roleId == null || roleId <= 0) {
            return List.of();
        }
        return permissionMapper.listByRoleId(roleId);
    }

    @Override
    public List<PermissionEntity> listByInternalUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return List.of();
        }
        return permissionMapper.listByInternalUserId(userId);
    }

    @Override
    public List<String> listPermissionCodesByInternalUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return List.of();
        }
        return permissionMapper.listPermissionCodesByInternalUserId(userId);
    }

    @Override
    public List<String> listPermissionCodesByUserType(String userType) {
        if (userType == null || userType.isBlank()) {
            return List.of();
        }
        return permissionMapper.listPermissionCodesByUserType(userType.trim());
    }
}
