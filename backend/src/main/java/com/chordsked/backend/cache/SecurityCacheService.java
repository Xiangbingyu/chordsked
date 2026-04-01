package com.chordsked.backend.cache;

import java.util.Date;
import java.util.List;

public interface SecurityCacheService {
    List<String> getAuthorityCodes(String userType, Long userId);

    void cacheAuthorityCodes(String userType, Long userId, List<String> codes);

    boolean isTokenRevoked(String token);

    boolean isTokenActive(String token);

    void markTokenActive(String token, Date expiration);

    void markTokenRevoked(String token, Date expiration);
}
