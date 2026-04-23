package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.config.properties.RoleProperties;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.dao.RolePermissionDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.role.RoleDeleteRequest;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.service.role.RoleDeleteService;
import com.chordsked.backend.utils.string.StringNormalizeUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service("roleDeleteService")
public class RoleDeleteServiceImpl implements RoleDeleteService {
    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "rolePermissionDao")
    private RolePermissionDao rolePermissionDao;

    @Resource(name = "roleProperties")
    private RoleProperties roleProperties;

    @Override
    @AuditLog(module = "ROLE_MANAGEMENT", action = "DELETE_ROLE")
    @Transactional(rollbackFor = Exception.class)
    public void delete(RoleDeleteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        Long roleId = request.getRoleId();
        if (roleId == null || roleId <= 0) {
            throw new IllegalArgumentException("roleId must be greater than 0");
        }
        RoleEntity role = roleDao.getById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色不存在");
        }
        if (isPresetRole(role)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统预置角色不能删除");
        }
        if (roleDao.countUserBinding(roleId) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色已绑定用户，不能删除");
        }
        rolePermissionDao.deleteByRoleId(roleId);
        roleDao.deleteById(roleId);
    }

    private boolean isPresetRole(RoleEntity role) {
        if (role == null || role.getCode() == null) {
            return false;
        }
        String roleCode = StringNormalizeUtils.trimToUpperCaseOrEmpty(role.getCode());
        return roleProperties.getPresetRoleCodes().stream()
                .filter(Objects::nonNull)
                .map(StringNormalizeUtils::trimToUpperCaseOrEmpty)
                .anyMatch(roleCode::equals);
    }
}
