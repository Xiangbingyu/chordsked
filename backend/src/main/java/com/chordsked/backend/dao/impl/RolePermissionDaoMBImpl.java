package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.RolePermissionDao;
import com.chordsked.backend.dao.mapper.RolePermissionMapper;
import com.chordsked.backend.model.entity.RolePermissionEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("rolePermissionDao")
public class RolePermissionDaoMBImpl implements RolePermissionDao {
    @Resource(name = "rolePermissionMapper")
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public RolePermissionEntity getById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return rolePermissionMapper.getById(id);
    }

    @Override
    public List<RolePermissionEntity> listByRoleId(Long roleId) {
        if (roleId == null || roleId <= 0) {
            return List.of();
        }
        return rolePermissionMapper.listByRoleId(roleId);
    }

    @Override
    public List<RolePermissionEntity> listByPermissionId(Long permissionId) {
        if (permissionId == null || permissionId <= 0) {
            return List.of();
        }
        return rolePermissionMapper.listByPermissionId(permissionId);
    }
}
