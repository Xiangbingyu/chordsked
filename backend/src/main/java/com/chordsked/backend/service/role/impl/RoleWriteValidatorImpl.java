package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.dao.PermissionDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.entity.PermissionEntity;
import com.chordsked.backend.model.enums.RoleStatus;
import com.chordsked.backend.service.role.RoleOperationGuardService;
import com.chordsked.backend.service.role.RoleWriteValidator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service("roleWriteValidator")
public class RoleWriteValidatorImpl implements RoleWriteValidator {
    @Resource(name = "permissionDao")
    private PermissionDao permissionDao;

    @Resource(name = "roleOperationGuardService")
    private RoleOperationGuardService roleOperationGuardService;

    @Override
    public String validateAndNormalizeCode(String code) {
        String normalizedCode = code == null ? null : code.trim();
        if (normalizedCode == null || normalizedCode.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色编码不能为空");
        }
        if (normalizedCode.length() > 50) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色编码长度不能超过50");
        }
        return normalizedCode;
    }

    @Override
    public String validateAndNormalizeName(String name) {
        String normalizedName = name == null ? null : name.trim();
        if (normalizedName == null || normalizedName.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色名称不能为空");
        }
        if (normalizedName.length() > 50) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色名称长度不能超过50");
        }
        return normalizedName;
    }

    @Override
    public void validateDescription(String description) {
        if (description != null && description.length() > 200) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色描述长度不能超过200");
        }
    }

    @Override
    public Integer validateStatus(Integer status) {
        if (RoleStatus.fromCode(status) == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态值无效");
        }
        return status;
    }

    @Override
    public List<Long> validatePermissionIds(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "permissionIds不能为空");
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
        return distinctPermissionIds;
    }

    @Override
    public void validateRoleCodeForCreate(String roleCode) {
        if (roleOperationGuardService.isProtectedRoleCode(roleCode)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统保护角色编码不允许创建");
        }
    }

    @Override
    public void validateRoleCodeForUpdate(String roleCode) {
        if (roleOperationGuardService.isProtectedRoleCode(roleCode)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统保护角色编码不允许使用");
        }
    }
}
