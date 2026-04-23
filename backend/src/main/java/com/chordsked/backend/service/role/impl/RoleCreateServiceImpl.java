package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.dao.PermissionDao;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.dao.RolePermissionDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.role.RoleCreateRequest;
import com.chordsked.backend.model.entity.PermissionEntity;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.model.entity.RolePermissionEntity;
import com.chordsked.backend.model.enums.RoleStatus;
import com.chordsked.backend.service.role.RoleCreateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service("roleCreateService")
public class RoleCreateServiceImpl implements RoleCreateService {
    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "permissionDao")
    private PermissionDao permissionDao;

    @Resource(name = "rolePermissionDao")
    private RolePermissionDao rolePermissionDao;

    @Override
    @AuditLog(module = "ROLE_MANAGEMENT", action = "CREATE_ROLE")
    @Transactional(rollbackFor = Exception.class)
    public Long create(RoleCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        Integer status = request.getStatus();
        if (RoleStatus.fromCode(status) == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "status is invalid");
        }
        String code = request.getCode() == null ? null : request.getCode().trim();
        String name = request.getName() == null ? null : request.getName().trim();
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (code.length() > 50) {
            throw new IllegalArgumentException("code length must be <= 50");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("name length must be <= 50");
        }
        String description = request.getDescription();
        if (description != null && description.length() > 200) {
            throw new IllegalArgumentException("description length must be <= 200");
        }
        List<Long> permissionIds = request.getPermissionIds();
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "permissionIds不能为空");
        }
        if (roleDao.getByCode(code) != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色编码已存在");
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
        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
        role.setStatus(status);
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        roleDao.save(role);

        List<RolePermissionEntity> rolePermissions = distinctPermissionIds.stream()
                .map(permissionId -> buildRolePermission(role.getId(), permissionId, now))
                .toList();
        rolePermissionDao.saveBatch(rolePermissions);
        return role.getId();
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
