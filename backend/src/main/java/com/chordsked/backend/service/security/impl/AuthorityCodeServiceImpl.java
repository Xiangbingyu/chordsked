package com.chordsked.backend.service.security.impl;

import com.chordsked.backend.config.properties.RoleProperties;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.PermissionDao;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.service.security.AuthorityCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("authorityCodeService")
public class AuthorityCodeServiceImpl implements AuthorityCodeService {
    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Resource(name = "permissionDao")
    private PermissionDao permissionDao;

    @Resource(name = "roleProperties")
    private RoleProperties roleProperties;

    @Override
    public List<String> getAuthorityCodes(AccountUserType userType, Long userId) {
        if (userType == null) {
            return List.of();
        }
        if (AccountUserType.ADMIN == userType) {
            if (isSystemAdmin(userId)) {
                return permissionDao.listPermissionCodesByUserType(userType.getCode());
            }
            return permissionDao.listPermissionCodesByInternalUserId(userId);
        }
        return permissionDao.listPermissionCodesByUserType(userType.getCode());
    }

    private boolean isSystemAdmin(Long userId) {
        if (userId == null || userId <= 0L) {
            return false;
        }
        List<String> protectedRoleCodes = roleProperties.getProtectedRoleCodes();
        return protectedRoleCodes.stream().anyMatch(roleCode -> internalUserDao.hasRoleCode(userId, roleCode));
    }
}
