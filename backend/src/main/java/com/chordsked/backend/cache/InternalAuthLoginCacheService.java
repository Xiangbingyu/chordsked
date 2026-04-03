package com.chordsked.backend.cache;

import com.chordsked.backend.model.entity.InternalUserEntity;

public interface InternalAuthLoginCacheService {
    InternalUserEntity getInternalUserLoginSnapshot(String username);

    void cacheInternalUserLoginSnapshot(InternalUserEntity internalUser);

    void clearInternalUserLoginSnapshot(String username);

    Integer getLoginFailCount(String username);

    Long getLockedUntil(String username);

    void recordLoginFailure(String username, int lockThreshold, long lockMinutes);

    void clearLoginState(String username);
}
