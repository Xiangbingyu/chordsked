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

/**
 * 安全相关缓存服务实现。
 * 维护三类缓存：
 * 1) 用户快照：缓存鉴权阶段装载 UserDetails 所需的最小用户状态；
 * 2) 权限快照：缓存用户权限码列表，降低鉴权阶段的重复查询；
 * 3) Token 状态：记录 token 当前是否有效或已撤销。
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

    /**
     * 读取 security 鉴权阶段使用的用户快照。
     * 仅包含 enabled 与 currentCampusId 两项最小字段，避免把完整用户实体放入安全缓存。
     */
    @Override
    public SecurityUserSnapshot getUserSnapshot(String userType, Long userId) {
        if (!redisProperties.isEnabled() || hasInvalidAuthorityCacheParameter(userType, userId)) {
            return null;
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(buildUserSnapshotKey(userType, userId));
            if (value == null || value.isBlank()) {
                return null;
            }
            String[] values = value.split("\\|", -1);
            if (values.length != 2) {
                return null;
            }
            boolean enabled = "1".equals(values[0]);
            Long currentCampusId = values[1].isBlank() ? null : Long.parseLong(values[1]);
            return new SecurityUserSnapshot(userType.trim(), userId, enabled, currentCampusId);
        } catch (RuntimeException exception) {
            logger.warn("Read user snapshot cache failed", exception);
            return null;
        }
    }

    /**
     * 写入 security 鉴权阶段使用的用户快照。
     * 缓存 Key 使用 userType + userId，避免与登录链路按 username 组织的缓存混用。
     */
    @Override
    public void cacheUserSnapshot(SecurityUserSnapshot userSnapshot) {
        if (!redisProperties.isEnabled() || hasInvalidUserSnapshot(userSnapshot)) {
            return;
        }
        try {
            String value = (userSnapshot.enabled() ? "1" : "0")
                    + "|"
                    + (userSnapshot.currentCampusId() == null ? "" : userSnapshot.currentCampusId());
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

    /**
     * 读取指定用户的权限码缓存。
     * 空结果使用固定标记缓存，避免反复穿透到下游数据源。
     */
    @Override
    public List<String> getAuthorityCodes(String userType, Long userId) {
        if (!redisProperties.isEnabled() || hasInvalidAuthorityCacheParameter(userType, userId)) {
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

    /**
     * 写入指定用户的权限码缓存。
     * 当权限为空时写入空标记，形成负缓存。
     */
    @Override
    public void cacheAuthorityCodes(String userType, Long userId, List<String> codes) {
        if (!redisProperties.isEnabled() || hasInvalidAuthorityCacheParameter(userType, userId)) {
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

    /**
     * 判断 token 是否已被撤销。
     */
    @Override
    public boolean isTokenRevoked(String token) {
        return hasTokenState(token, TOKEN_STATE_REVOKED, false, true);
    }

    /**
     * 判断 token 当前是否处于有效状态。
     */
    @Override
    public boolean isTokenActive(String token) {
        return hasTokenState(token, TOKEN_STATE_ACTIVE, true, false);
    }

    /**
     * 标记 token 为有效状态，并按过期时间写入 TTL。
     */
    @Override
    public void markTokenActive(String token, Date expiration) {
        cacheTokenState(token, expiration, TOKEN_STATE_ACTIVE);
    }

    /**
     * 标记 token 为撤销状态，并沿用剩余有效期作为缓存 TTL。
     */
    @Override
    public void markTokenRevoked(String token, Date expiration) {
        cacheTokenState(token, expiration, TOKEN_STATE_REVOKED);
    }

    /**
     * 写入 token 状态缓存。
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
     * 统一判断 token 是否处于目标状态。
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
     * 构建权限缓存 Key。
     */
    private String buildAuthorityKey(String userType, Long userId) {
        return AUTHORITY_KEY_PREFIX + userType.trim() + ":" + userId;
    }

    /**
     * 构建 security 用户快照缓存 Key。
     */
    private String buildUserSnapshotKey(String userType, Long userId) {
        return USER_SNAPSHOT_KEY_PREFIX + userType.trim() + ":" + userId;
    }

    /**
     * 构建 token 状态缓存 Key。
     * 使用 token 摘要，避免原始 token 直接作为 Redis key。
     */
    private String buildTokenKey(String token) {
        return TOKEN_KEY_PREFIX + DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 校验权限缓存相关参数是否合法。
     */
    private boolean hasInvalidAuthorityCacheParameter(String userType, Long userId) {
        return userType == null || userType.isBlank() || userId == null || userId <= 0L;
    }

    /**
     * 校验用户快照写入参数是否合法。
     */
    private boolean hasInvalidUserSnapshot(SecurityUserSnapshot userSnapshot) {
        return userSnapshot == null
                || hasInvalidAuthorityCacheParameter(userSnapshot.userType(), userSnapshot.userId());
    }
}
