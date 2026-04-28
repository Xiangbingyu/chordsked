package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.dao.RolePermissionDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.role.RoleUpdateRequest;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.model.entity.RolePermissionEntity;
import com.chordsked.backend.service.role.RoleCacheCleanupService;
import com.chordsked.backend.service.role.RoleOperationGuardService;
import com.chordsked.backend.service.role.RoleUpdateService;
import com.chordsked.backend.service.role.RoleWriteValidator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service("roleUpdateService")
public class RoleUpdateServiceImpl implements RoleUpdateService {
    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "rolePermissionDao")
    private RolePermissionDao rolePermissionDao;

    @Resource(name = "roleOperationGuardService")
    private RoleOperationGuardService roleOperationGuardService;

    @Resource(name = "roleWriteValidator")
    private RoleWriteValidator roleWriteValidator;

    @Resource(name = "roleCacheCleanupService")
    private RoleCacheCleanupService roleCacheCleanupService;

    @Override
    @AuditLog(module = "ROLE_MANAGEMENT", action = "UPDATE_ROLE")
    @Transactional(rollbackFor = Exception.class)
    public void update(RoleUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请求参数不能为空");
        }
        Long roleId = request.getRoleId();
        RoleEntity existingRole = roleOperationGuardService.validateOperationTarget(roleId, "修改");
        roleOperationGuardService.validateRoleCanBeUpdated(existingRole);
        String roleName = roleWriteValidator.validateAndNormalizeName(request.getName());
        String description = request.getDescription();
        roleWriteValidator.validateDescription(description);
        Integer status = roleWriteValidator.validateStatus(request.getStatus());
        List<Long> distinctPermissionIds = roleWriteValidator.validatePermissionIds(request.getPermissionIds());
        String roleCodeInput = request.getCode();
        String roleCode = (roleCodeInput == null || roleCodeInput.isBlank())
                ? existingRole.getCode()
                : roleWriteValidator.validateAndNormalizeCode(roleCodeInput);
        roleWriteValidator.validateRoleCodeForUpdate(roleCode);

        RoleEntity duplicateRole = roleDao.getByCode(roleCode);
        if (duplicateRole != null && !Objects.equals(duplicateRole.getId(), roleId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色编码已存在");
        }
        roleCacheCleanupService.cleanupAuthorityByRoleIdAfterCommit(roleId);

        long now = System.currentTimeMillis();
        RoleEntity role = new RoleEntity();
        role.setId(roleId);
        role.setCode(roleCode);
        role.setName(roleName);
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
