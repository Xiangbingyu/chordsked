package com.chordsked.backend.service.internaluser;

import java.util.List;

public interface InternalUserCacheCleanupService {
    void cleanupAfterProfileUpdated(Long userId, String username, String phone);

    void cleanupAfterStatusEnabled(Long userId, String username, String phone);

    void cleanupAfterStatusDisabled(Long userId, String username, String phone);

    void cleanupAfterPasswordReset(Long userId, String username, String phone);

    void cleanupAuthorityByUserIds(List<Long> userIds);
}
