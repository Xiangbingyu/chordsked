package com.chordsked.backend.service.role;

public interface RoleCacheCleanupService {
    void cleanupAuthorityByRoleIdAfterCommit(Long roleId);
}
