package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.config.properties.RoleProperties;
import com.chordsked.backend.dao.CampusDao;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.model.enums.RoleStatus;
import com.chordsked.backend.service.internaluser.InternalUserWriteValidator;
import com.chordsked.backend.utils.normalize.StringNormalizeUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service("internalUserWriteValidator")
public class InternalUserWriteValidatorImpl implements InternalUserWriteValidator {
    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "campusDao")
    private CampusDao campusDao;

    @Resource(name = "roleProperties")
    private RoleProperties roleProperties;

    @Override
    public List<Long> validateRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "roleIds不能为空");
        }
        List<Long> distinctRoleIds = roleIds.stream()
                .filter(Objects::nonNull)
                .filter(roleId -> roleId > 0)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf
                ));
        if (distinctRoleIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "roleIds不能为空");
        }

        Set<String> protectedRoleCodes = resolveProtectedRoleCodes();
        for (Long roleId : distinctRoleIds) {
            RoleEntity role = roleDao.getById(roleId);
            if (role == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "角色不存在: " + roleId);
            }
            if (role.getStatusEnum() != RoleStatus.ENABLED) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "角色未启用: " + roleId);
            }
            if (protectedRoleCodes.contains(StringNormalizeUtils.normalizeOrEmpty(role.getCode()))) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "受保护角色不能分配给普通账号");
            }
        }
        return distinctRoleIds;
    }

    @Override
    public List<Long> validateCampusIds(List<Long> campusIds, Long primaryCampusId) {
        if (campusIds == null || campusIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "campusIds不能为空");
        }
        if (primaryCampusId == null || primaryCampusId <= 0) {
            throw new IllegalArgumentException("primaryCampusId must be greater than 0");
        }
        if (!campusIds.contains(primaryCampusId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "主校区必须在校区列表中");
        }
        List<Long> distinctCampusIds = campusIds.stream()
                .filter(Objects::nonNull)
                .filter(campusId -> campusId > 0)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf
                ));
        if (distinctCampusIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "campusIds不能为空");
        }
        if (!distinctCampusIds.contains(primaryCampusId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "主校区必须在校区列表中");
        }
        for (Long campusId : distinctCampusIds) {
            if (campusDao.getById(campusId) == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "校区不存在: " + campusId);
            }
        }
        return distinctCampusIds;
    }

    private Set<String> resolveProtectedRoleCodes() {
        return roleProperties.getProtectedRoleCodes().stream()
                .filter(Objects::nonNull)
                .map(StringNormalizeUtils::normalizeOrEmpty)
                .filter(code -> !code.isEmpty())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
