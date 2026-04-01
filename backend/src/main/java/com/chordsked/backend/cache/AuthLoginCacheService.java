package com.chordsked.backend.cache;

import com.chordsked.backend.model.entity.InternalUserEntity;

public interface AuthLoginCacheService {
    InternalUserEntity getInternalUserLoginSnapshot(String username);

    void cacheInternalUserLoginSnapshot(InternalUserEntity internalUser);

    void evictInternalUserLoginSnapshot(String username);
}
