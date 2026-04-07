package com.chordsked.backend.cache.auth.provider;

import com.chordsked.backend.model.auth.AuthLoginSnapshot;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.enums.AccountUserType;

public interface AuthLoginSnapshotCacheProvider {
    AuthLoginMethod getLoginMethod();

    AuthLoginSnapshot getLoginSnapshot(AccountUserType userType, String principal);

    void cacheLoginSnapshot(AuthLoginSnapshot snapshot);

    void clearLoginSnapshot(AccountUserType userType, String principal);
}
