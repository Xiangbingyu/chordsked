package com.chordsked.backend.cache.security;

import com.chordsked.backend.model.enums.UserDataScopeType;

import java.util.Date;
import java.util.List;

public interface SecurityCacheService {
    SecurityUserSnapshot getUserSnapshot(String userType, Long userId);

    void cacheUserSnapshot(SecurityUserSnapshot userSnapshot);

    List<String> getAuthorityCodes(String userType, Long userId);

    void cacheAuthorityCodes(String userType, Long userId, List<String> codes);

    void clearUserSnapshot(String userType, Long userId);

    void clearAuthorityCodes(String userType, Long userId);

    boolean isTokenRevoked(String token);

    boolean isTokenActive(String token);

    void markTokenActive(String token, Date expiration);

    void markTokenRevoked(String token, Date expiration);

    record SecurityUserSnapshot(
            String userType,
            Long userId,
            boolean enabled,
            Long currentCampusId,
            UserDataScopeType dataScopeType
    ) {
    }
}
