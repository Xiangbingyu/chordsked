package com.chordsked.backend.cache.impl;

import com.chordsked.backend.cache.AuthLoginCacheService;
import com.chordsked.backend.config.properties.RedisProperties;
import com.chordsked.backend.model.entity.InternalUserEntity;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component("authLoginCacheService")
public class AuthLoginCacheServiceImpl implements AuthLoginCacheService {
    private static final Logger logger = LoggerFactory.getLogger(AuthLoginCacheServiceImpl.class);
    private static final String INTERNAL_USER_LOGIN_KEY_PREFIX = "chordsked:auth:internal-user:";

    @Resource(name = "redisProperties")
    private RedisProperties redisProperties;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public InternalUserEntity getInternalUserLoginSnapshot(String username) {
        if (!redisProperties.isEnabled() || username == null || username.isBlank()) {
            return null;
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(buildInternalUserLoginKey(username));
            if (value == null || value.isBlank()) {
                return null;
            }
            String[] values = value.split("\\|", -1);
            if (values.length != 7) {
                return null;
            }
            Long userId = Long.parseLong(values[0]);
            String password = values[1];
            Integer status = Integer.parseInt(values[2]);
            Integer mustChangePassword = Integer.parseInt(values[3]);
            Integer loginFailCount = Integer.parseInt(values[4]);
            LocalDateTime lockedUntil = values[5].isBlank() ? null : LocalDateTime.parse(values[5]);
            Integer dataScopeType = Integer.parseInt(values[6]);
            InternalUserEntity internalUser = new InternalUserEntity();
            internalUser.setId(userId);
            internalUser.setUsername(username.trim());
            internalUser.setPassword(password);
            internalUser.setStatus(status);
            internalUser.setMustChangePassword(mustChangePassword);
            internalUser.setLoginFailCount(loginFailCount);
            internalUser.setLockedUntil(lockedUntil);
            internalUser.setDataScopeType(dataScopeType);
            return internalUser;
        } catch (RuntimeException exception) {
            logger.warn("Read internal user cache failed", exception);
            return null;
        }
    }

    @Override
    public void cacheInternalUserLoginSnapshot(InternalUserEntity internalUser) {
        if (!redisProperties.isEnabled()
                || internalUser == null
                || internalUser.getUsername() == null
                || internalUser.getUsername().isBlank()
                || internalUser.getId() == null
                || internalUser.getPassword() == null
                || internalUser.getStatus() == null
                || internalUser.getMustChangePassword() == null
                || internalUser.getLoginFailCount() == null
                || internalUser.getDataScopeType() == null) {
            return;
        }
        try {
            String value = String.join(
                    "|",
                    String.valueOf(internalUser.getId()),
                    internalUser.getPassword(),
                    String.valueOf(internalUser.getStatus()),
                    String.valueOf(internalUser.getMustChangePassword()),
                    String.valueOf(internalUser.getLoginFailCount()),
                    internalUser.getLockedUntil() == null ? "" : internalUser.getLockedUntil().toString(),
                    String.valueOf(internalUser.getDataScopeType())
            );
            Duration ttl = Duration.ofSeconds(Math.max(redisProperties.getAuthorityCacheTtlSeconds(), 1L));
            stringRedisTemplate.opsForValue().set(buildInternalUserLoginKey(internalUser.getUsername()), value, ttl);
        } catch (RuntimeException exception) {
            logger.warn("Write internal user cache failed", exception);
        }
    }

    @Override
    public void evictInternalUserLoginSnapshot(String username) {
        if (!redisProperties.isEnabled() || username == null || username.isBlank()) {
            return;
        }
        try {
            stringRedisTemplate.delete(buildInternalUserLoginKey(username));
        } catch (RuntimeException exception) {
            logger.warn("Delete internal user cache failed", exception);
        }
    }

    private String buildInternalUserLoginKey(String username) {
        String normalizedUsername = username.trim().toLowerCase();
        return INTERNAL_USER_LOGIN_KEY_PREFIX + normalizedUsername;
    }
}
