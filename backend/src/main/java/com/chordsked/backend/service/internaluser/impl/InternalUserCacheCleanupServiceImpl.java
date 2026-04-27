package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.cache.auth.AuthLoginStateCacheService;
import com.chordsked.backend.cache.auth.provider.AuthLoginSnapshotCacheProvider;
import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.service.internaluser.InternalUserCacheCleanupService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("internalUserCacheCleanupService")
public class InternalUserCacheCleanupServiceImpl implements InternalUserCacheCleanupService {
    @Resource(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @Resource(name = "authLoginStateCacheService")
    private AuthLoginStateCacheService authLoginStateCacheService;

    @Resource
    private List<AuthLoginSnapshotCacheProvider> authLoginSnapshotCacheProviders;

    @Override
    public void cleanupAfterProfileUpdated(Long userId, String username, String phone) {
        clearSecurityAndAuthCaches(userId, username, phone, true);
    }

    @Override
    public void cleanupAfterStatusEnabled(Long userId, String username, String phone) {
        clearSecurityAndAuthCaches(userId, username, phone, false);
    }

    @Override
    public void cleanupAfterStatusDisabled(Long userId, String username, String phone) {
        clearSecurityAndAuthCaches(userId, username, phone, true);
    }

    @Override
    public void cleanupAfterPasswordReset(Long userId, String username, String phone) {
        clearSecurityAndAuthCaches(userId, username, phone, false);
    }

    @Override
    public void cleanupAuthorityByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        String userType = AccountUserType.ADMIN.getCode();
        userIds.stream()
                .filter(java.util.Objects::nonNull)
                .filter(userId -> userId > 0)
                .distinct()
                .forEach(userId -> securityCacheService.clearAuthorityCodes(userType, userId));
    }

    private void clearSecurityAndAuthCaches(Long userId, String username, String phone, boolean clearAuthority) {
        String userType = AccountUserType.ADMIN.getCode();
        securityCacheService.clearUserSnapshot(userType, userId);
        if (clearAuthority) {
            securityCacheService.clearAuthorityCodes(userType, userId);
        }
        clearPrincipalCaches(AccountUserType.ADMIN, username);
        clearPrincipalCaches(AccountUserType.ADMIN, phone);
    }

    private void clearPrincipalCaches(AccountUserType userType, String principal) {
        if (principal == null || principal.isBlank()) {
            return;
        }
        authLoginStateCacheService.clearLoginState(userType, principal);
        for (AuthLoginSnapshotCacheProvider provider : authLoginSnapshotCacheProviders) {
            provider.clearLoginSnapshot(userType, principal);
        }
    }
}
