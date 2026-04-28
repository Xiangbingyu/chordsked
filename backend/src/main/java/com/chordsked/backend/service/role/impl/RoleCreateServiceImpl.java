package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.dao.RolePermissionDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.role.RoleCreateRequest;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.model.entity.RolePermissionEntity;
import com.chordsked.backend.service.role.RoleCreateService;
import com.chordsked.backend.service.role.RoleWriteValidator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("roleCreateService")
public class RoleCreateServiceImpl implements RoleCreateService {
    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "rolePermissionDao")
    private RolePermissionDao rolePermissionDao;

    @Resource(name = "roleWriteValidator")
    private RoleWriteValidator roleWriteValidator;

    @Override
    @AuditLog(module = "ROLE_MANAGEMENT", action = "CREATE_ROLE")
    @Transactional(rollbackFor = Exception.class)
    public Long create(RoleCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请求参数不能为空");
        }
        Integer status = roleWriteValidator.validateStatus(request.getStatus());
        String code = roleWriteValidator.validateAndNormalizeCode(request.getCode());
        String name = roleWriteValidator.validateAndNormalizeName(request.getName());
        String description = request.getDescription();
        roleWriteValidator.validateDescription(description);
        roleWriteValidator.validateRoleCodeForCreate(code);
        if (roleDao.getByCode(code) != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色编码已存在");
        }
        List<Long> distinctPermissionIds = roleWriteValidator.validatePermissionIds(request.getPermissionIds());

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
