package com.chordsked.backend.service.security.impl;

import com.chordsked.backend.dao.PermissionDao;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.service.security.AuthorityCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("authorityCodeService")
public class AuthorityCodeServiceImpl implements AuthorityCodeService {
    @Resource(name = "permissionDao")
    private PermissionDao permissionDao;

    @Override
    public List<String> getAuthorityCodes(AccountUserType userType, Long userId) {
        if (userType == null) {
            return List.of();
        }
        if (AccountUserType.ADMIN == userType) {
            return permissionDao.listPermissionCodesByInternalUserId(userId);
        }
        return permissionDao.listPermissionCodesByUserType(userType.getCode());
    }
}
