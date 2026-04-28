package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.dao.UserRoleDao;
import com.chordsked.backend.service.internaluser.InternalUserCacheCleanupService;
import com.chordsked.backend.service.role.RoleCacheCleanupService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service("roleCacheCleanupService")
public class RoleCacheCleanupServiceImpl implements RoleCacheCleanupService {
    @Resource(name = "userRoleDao")
    private UserRoleDao userRoleDao;

    @Resource(name = "internalUserCacheCleanupService")
    private InternalUserCacheCleanupService internalUserCacheCleanupService;

    @Override
    public void cleanupAuthorityByRoleIdAfterCommit(Long roleId) {
        if (roleId == null || roleId <= 0) {
            return;
        }
        List<Long> userIds = userRoleDao.listUserIdsByRoleId(roleId);
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    internalUserCacheCleanupService.cleanupAuthorityByUserIds(userIds);
                }
            });
            return;
        }
        internalUserCacheCleanupService.cleanupAuthorityByUserIds(userIds);
    }
}
