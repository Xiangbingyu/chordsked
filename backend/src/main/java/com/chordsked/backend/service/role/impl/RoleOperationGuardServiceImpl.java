package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.config.properties.RoleProperties;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.service.role.RoleOperationGuardService;
import com.chordsked.backend.utils.normalize.StringNormalizeUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("roleOperationGuardService")
public class RoleOperationGuardServiceImpl implements RoleOperationGuardService {
    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "roleProperties")
    private RoleProperties roleProperties;

    @Override
    public RoleEntity validateOperationTarget(Long roleId, String actionName) {
        if (roleId == null || roleId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "roleId必须大于0");
        }
        RoleEntity role = roleDao.getById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色不存在");
        }
        return role;
    }

    @Override
    public void validateRoleCanBeUpdated(RoleEntity role) {
        if (role == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色不存在");
        }
        if (isProtectedRoleCode(role.getCode())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统保护角色不允许修改");
        }
    }

    @Override
    public void validateRoleCanBeDeleted(RoleEntity role) {
        if (role == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色不存在");
        }
        if (isProtectedRoleCode(role.getCode())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统保护角色不能删除");
        }
    }

    @Override
    public boolean isProtectedRoleCode(String roleCode) {
        String normalizedRoleCode = StringNormalizeUtils.normalizeOrEmpty(roleCode);
        return roleProperties.getProtectedRoleCodes().stream()
                .filter(Objects::nonNull)
                .map(StringNormalizeUtils::normalizeOrEmpty)
                .filter(code -> !code.isEmpty())
                .anyMatch(normalizedRoleCode::equals);
    }

}
