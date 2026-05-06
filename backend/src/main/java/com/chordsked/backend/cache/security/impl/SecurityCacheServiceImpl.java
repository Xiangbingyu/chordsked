package com.chordsked.backend.cache.security.impl;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.config.properties.RedisProperties;
import com.chordsked.backend.model.enums.UserDataScopeType;
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

/**
 * 安全缓存服务实现。
 * 统一负责用户快照、权限码与令牌状态三类安全相关缓存的读写，
 * 使安全认证链路在开启 Redis 时可以复用缓存结果，降低重复计算和数据库访问。
 */
@Component("securityCacheService")
public class SecurityCacheServiceImpl implements SecurityCacheService {
    private static final Logger logger = LoggerFactory.getLogger(SecurityCacheServiceImpl.class);
    private static final String USER_SNAPSHOT_KEY_PREFIX = "chordsked:security:user:";
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
    /**
     * 读取安全用户快照缓存。
     * 当前缓存 enabled 状态、primaryOrgNodeId 与 dataScopeType，用于鉴权链路中的快速判定。
     */
    public SecurityUserSnapshot getUserSnapshot(String userType, Long userId) {
        if (!redisProperties.isEnabled() || hasInvalidAuthorityCacheParameters(userType, userId)) {
            return null;
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(buildUserSnapshotKey(userType, userId));
            if (value == null || value.isBlank()) {
                return null;
            }
            String[] values = value.split("\\|", -1);
            if (values.length < 2) {
                return null;
            }
            boolean enabled = "1".equals(values[0]);
            Long primaryOrgNodeId;
            UserDataScopeType dataScopeType = null;
            if (values.length >= 4) {
                primaryOrgNodeId = values[2].isBlank() ? null : Long.parseLong(values[2]);
                if (!values[3].isBlank()) {
                    dataScopeType = UserDataScopeType.fromCode(Integer.parseInt(values[3]));
                }
            } else {
                primaryOrgNodeId = values[1].isBlank() ? null : Long.parseLong(values[1]);
                if (values.length >= 3 && !values[2].isBlank()) {
                    dataScopeType = UserDataScopeType.fromCode(Integer.parseInt(values[2]));
                }
            }
            return new SecurityUserSnapshot(userType.trim(), userId, enabled, primaryOrgNodeId, dataScopeType);
        } catch (RuntimeException exception) {
            logger.warn("Read user snapshot cache failed", exception);
            return null;
        }
    }

    @Override
    /**
     * 写入安全用户快照缓存。
     */
    public void cacheUserSnapshot(SecurityUserSnapshot userSnapshot) {
        if (!redisProperties.isEnabled() || hasInvalidUserSnapshot(userSnapshot)) {
            return;
        }
        try {
            String value = (userSnapshot.enabled() ? "1" : "0")
                    + "|"
                    + (userSnapshot.primaryOrgNodeId() == null ? "" : userSnapshot.primaryOrgNodeId())
                    + "|"
                    + (userSnapshot.dataScopeType() == null ? "" : userSnapshot.dataScopeType().getCode());
            Duration ttl = Duration.ofSeconds(Math.max(redisProperties.getUserSnapshotCacheTtlSeconds(), 1L));
            stringRedisTemplate.opsForValue().set(
                    buildUserSnapshotKey(userSnapshot.userType(), userSnapshot.userId()),
                    value,
                    ttl
            );
        } catch (RuntimeException exception) {
            logger.warn("Write user snapshot cache failed", exception);
        }
    }

    @Override
    public void clearUserSnapshot(String userType, Long userId) {
        if (!redisProperties.isEnabled() || hasInvalidAuthorityCacheParameters(userType, userId)) {
            return;
        }
        try {
            stringRedisTemplate.delete(buildUserSnapshotKey(userType, userId));
        } catch (RuntimeException exception) {
            logger.warn("Clear user snapshot cache failed", exception);
        }
    }

    @Override
    /**
     * 读取权限码缓存。
     * 若缓存中记录的是空权限标记，则返回空列表，避免反复回源查询无权限用户。
     */
    public List<String> getAuthorityCodes(String userType, Long userId) {
        if (!redisProperties.isEnabled() || hasInvalidAuthorityCacheParameters(userType, userId)) {
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
    /**
     * 写入权限码缓存。
     * 对空权限列表使用固定占位符，便于和“未命中缓存”状态区分。
     */
    public void cacheAuthorityCodes(String userType, Long userId, List<String> codes) {
        if (!redisProperties.isEnabled() || hasInvalidAuthorityCacheParameters(userType, userId)) {
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
    public void clearAuthorityCodes(String userType, Long userId) {
        if (!redisProperties.isEnabled() || hasInvalidAuthorityCacheParameters(userType, userId)) {
            return;
        }
        try {
            stringRedisTemplate.delete(buildAuthorityKey(userType, userId));
        } catch (RuntimeException exception) {
            logger.warn("Clear authority cache failed", exception);
        }
    }

    @Override
    public boolean isTokenRevoked(String token) {
        return hasTokenState(token, TOKEN_STATE_REVOKED, false, true);
    }

    @Override
    public boolean isTokenActive(String token) {
        return hasTokenState(token, TOKEN_STATE_ACTIVE, true, false);
    }

    @Override
    public void markTokenActive(String token, Date expiration) {
        cacheTokenState(token, expiration, TOKEN_STATE_ACTIVE);
    }

    @Override
    public void markTokenRevoked(String token, Date expiration) {
        cacheTokenState(token, expiration, TOKEN_STATE_REVOKED);
    }

    /**
     * 写入令牌状态缓存。
     * 令牌缓存 TTL 与 JWT 剩余生命周期保持一致，避免缓存早于令牌失效或长期残留。
     */
    private void cacheTokenState(String token, Date expiration, String tokenState) {
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

    /**
     * 按预期状态检查令牌缓存。
     * 该方法同时处理“Redis 未启用”“会话缓存未开启”“token 为空”等默认行为分支。
     */
    private boolean hasTokenState(
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

    /**
     * 构建权限缓存 key。
     */
    private String buildAuthorityKey(String userType, Long userId) {
        return AUTHORITY_KEY_PREFIX + userType.trim() + ":" + userId;
    }

    /**
     * 构建用户快照缓存 key。
     */
    private String buildUserSnapshotKey(String userType, Long userId) {
        return USER_SNAPSHOT_KEY_PREFIX + userType.trim() + ":" + userId;
    }

    /**
     * 构建令牌状态缓存 key。
     * 令牌原文不直接作为 key，而是先做 MD5，避免缓存键过长并减少敏感信息暴露。
     */
    private String buildTokenKey(String token) {
        return TOKEN_KEY_PREFIX + DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 校验权限与用户快照缓存公用的基础参数。
     */
    private boolean hasInvalidAuthorityCacheParameters(String userType, Long userId) {
        return userType == null || userType.isBlank() || userId == null || userId <= 0L;
    }

    /**
     * 校验待写入的用户快照是否完整。
     */
    private boolean hasInvalidUserSnapshot(SecurityUserSnapshot userSnapshot) {
        return userSnapshot == null
                || hasInvalidAuthorityCacheParameters(userSnapshot.userType(), userSnapshot.userId());
    }
}
