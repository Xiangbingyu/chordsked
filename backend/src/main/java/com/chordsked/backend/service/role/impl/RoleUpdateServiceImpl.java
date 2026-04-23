package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.dao.PermissionDao;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.dao.RolePermissionDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.role.RoleUpdateRequest;
import com.chordsked.backend.model.entity.PermissionEntity;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.model.entity.RolePermissionEntity;
import com.chordsked.backend.model.enums.RoleStatus;
import com.chordsked.backend.service.role.RoleUpdateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service("roleUpdateService")
public class RoleUpdateServiceImpl implements RoleUpdateService {
    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "permissionDao")
    private PermissionDao permissionDao;

    @Resource(name = "rolePermissionDao")
    private RolePermissionDao rolePermissionDao;

    @Override
    @AuditLog(module = "ROLE_MANAGEMENT", action = "UPDATE_ROLE")
    @Transactional(rollbackFor = Exception.class)
    public void update(RoleUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        Long roleId = request.getRoleId();
        if (roleId == null || roleId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "roleId必须大于0");
        }
        String roleName = request.getName();
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (roleName.length() > 50) {
            throw new IllegalArgumentException("name length must be <= 50");
        }
        String description = request.getDescription();
        if (description != null && description.length() > 200) {
            throw new IllegalArgumentException("description length must be <= 200");
        }
        Integer status = request.getStatus();
        if (RoleStatus.fromCode(status) == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "status is invalid");
        }
        List<Long> permissionIds = request.getPermissionIds();
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "permissionIds不能为空");
        }
        RoleEntity existingRole = roleDao.getById(roleId);
        if (existingRole == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色不存在");
        }
        List<Long> distinctPermissionIds = permissionIds.stream()
                .filter(Objects::nonNull)
                .filter(permissionId -> permissionId > 0)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf
                ));
        if (distinctPermissionIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "permissionIds不能为空");
        }
        List<PermissionEntity> permissions = permissionDao.listByIds(distinctPermissionIds);
        if (permissions.size() != distinctPermissionIds.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "存在无效权限");
        }

        long now = System.currentTimeMillis();
        RoleEntity role = new RoleEntity();
        role.setId(roleId);
        role.setName(roleName.trim());
        role.setDescription(description);
        role.setStatus(status);
        role.setUpdatedAt(now);
        roleDao.updateById(role);

        rolePermissionDao.deleteByRoleId(roleId);
        List<RolePermissionEntity> rolePermissions = distinctPermissionIds.stream()
                .map(permissionId -> buildRolePermission(roleId, permissionId, now))
                .toList();
        rolePermissionDao.saveBatch(rolePermissions);
    }

    private RolePermissionEntity buildRolePermission(Long roleId, Long permissionId, Long now) {
        RolePermissionEntity rolePermission = new RolePermissionEntity();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        rolePermission.setCreatedAt(now);
        rolePermission.setUpdatedAt(now);
        return rolePermission;
    }
}
