package com.chordsked.backend.cache.impl;

import com.chordsked.backend.cache.SecurityCacheService;
import com.chordsked.backend.config.properties.RedisProperties;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component("securityCacheService")
public class SecurityCacheServiceImpl implements SecurityCacheService {
    private static final Logger logger = LoggerFactory.getLogger(SecurityCacheServiceImpl.class);
    private static final String AUTHORITY_KEY_PREFIX = "chordsked:security:authority:";
    private static final String AUTHORITY_EMPTY_MARKER = "__EMPTY__";
    private static final String TOKEN_KEY_PREFIX = "chordsked:security:token:";
    private static final String TOKEN_STATE_ACTIVE = "1";
    private static final String TOKEN_STATE_REVOKED = "0";

    @Resource(name = "redisProperties")
    private RedisProperties redisProperties;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<String> getAuthorityCodes(String userType, Long userId) {
        if (!redisProperties.isEnabled()) {
            return Collections.emptyList();
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(buildAuthorityKey(userType, userId));
            if (value == null || value.isBlank()) {
                return Collections.emptyList();
            }
            if (AUTHORITY_EMPTY_MARKER.equals(value)) {
                return Collections.emptyList();
            }
            return Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(code -> !code.isEmpty())
                    .collect(Collectors.toList());
        } catch (RuntimeException exception) {
            logger.warn("Read authority cache failed", exception);
            return Collections.emptyList();
        }
    }

    @Override
    public void cacheAuthorityCodes(String userType, Long userId, List<String> codes) {
        if (!redisProperties.isEnabled()) {
            return;
        }
        try {
            String value = (codes == null || codes.isEmpty()) ? AUTHORITY_EMPTY_MARKER : String.join(",", codes);
            Duration ttl = Duration.ofSeconds(Math.max(redisProperties.getAuthorityCacheTtlSeconds(), 1L));
            stringRedisTemplate.opsForValue().set(buildAuthorityKey(userType, userId), value, ttl);
        } catch (RuntimeException exception) {
            logger.warn("Write authority cache failed", exception);
        }
    }

    @Override
    public boolean isTokenRevoked(String token) {
        return isTokenState(token, TOKEN_STATE_REVOKED, false, true);
    }

    @Override
    public boolean isTokenActive(String token) {
        return isTokenState(token, TOKEN_STATE_ACTIVE, true, false);
    }

    @Override
    public void markTokenActive(String token, Date expiration) {
        writeTokenState(token, expiration, TOKEN_STATE_ACTIVE);
    }

    @Override
    public void markTokenRevoked(String token, Date expiration) {
        writeTokenState(token, expiration, TOKEN_STATE_REVOKED);
    }

    private void writeTokenState(String token, Date expiration, String tokenState) {
        if (!redisProperties.isEnabled() || !redisProperties.isTokenSessionEnabled()) {
            return;
        }
        if (token == null || token.isBlank() || expiration == null) {
            return;
        }
        long ttlSeconds = Duration.between(Instant.now(), expiration.toInstant()).getSeconds();
        if (ttlSeconds <= 0) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(
                    buildTokenKey(token),
                    tokenState,
                    Duration.ofSeconds(ttlSeconds)
            );
        } catch (RuntimeException exception) {
            logger.warn("Write token state failed", exception);
        }
    }

    private boolean isTokenState(
            String token,
            String expectedState,
            boolean defaultWhenSessionDisabled,
            boolean defaultWhenTokenBlank
    ) {
        if (!redisProperties.isEnabled() || !redisProperties.isTokenSessionEnabled()) {
            return defaultWhenSessionDisabled;
        }
        if (token == null || token.isBlank()) {
            return defaultWhenTokenBlank;
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(buildTokenKey(token));
            return expectedState.equals(value);
        } catch (RuntimeException exception) {
            logger.warn("Read token state failed", exception);
            return false;
        }
    }

    private String buildAuthorityKey(String userType, Long userId) {
        return AUTHORITY_KEY_PREFIX + userType + ":" + userId;
    }

    private String buildTokenKey(String token) {
        return TOKEN_KEY_PREFIX + DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
    }
}
