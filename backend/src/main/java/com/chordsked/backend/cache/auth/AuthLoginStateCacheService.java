package com.chordsked.backend.cache.auth;

import com.chordsked.backend.model.enums.AccountUserType;

public interface AuthLoginStateCacheService {
    Integer getLoginFailCount(AccountUserType userType, String principal);

    Long getLockedUntil(AccountUserType userType, String principal);

    void recordLoginFailure(AccountUserType userType, String principal, int lockThreshold, long lockMinutes);

    void clearLoginState(AccountUserType userType, String principal);
}
