package com.chordsked.backend.cache.impl;

import com.chordsked.backend.cache.InternalAuthLoginCacheService;
import com.chordsked.backend.config.properties.RedisProperties;
import com.chordsked.backend.model.entity.InternalUserEntity;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 内部账号登录相关缓存服务实现。
 * 维护两类缓存：
 * 1) 登录快照：用于减少登录场景下对用户表的重复查询；
 * 2) 登录状态：用于记录连续失败次数与锁定截止时间。
 */
@Component("internalAuthLoginCacheService")
public class InternalAuthLoginCacheServiceImpl implements InternalAuthLoginCacheService {
    private static final Logger logger = LoggerFactory.getLogger(InternalAuthLoginCacheServiceImpl.class);
    private static final String INTERNAL_USER_LOGIN_KEY_PREFIX = "chordsked:auth:internal-user:";
    private static final String INTERNAL_USER_LOGIN_STATE_KEY_PREFIX = "chordsked:auth:internal-user:state:";

    @Resource(name = "redisProperties")
    private RedisProperties redisProperties;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 读取内部账号登录快照。
     * 缓存结构：id|password|status|mustChangePassword|dataScopeType
     */
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
            if (values.length != 5) {
                return null;
            }
            Long userId = Long.parseLong(values[0]);
            String password = values[1];
            Integer status = Integer.parseInt(values[2]);
            Integer mustChangePassword = Integer.parseInt(values[3]);
            Integer dataScopeType = Integer.parseInt(values[4]);
            InternalUserEntity internalUser = new InternalUserEntity();
            internalUser.setId(userId);
            internalUser.setUsername(username.trim());
            internalUser.setPassword(password);
            internalUser.setStatus(status);
            internalUser.setMustChangePassword(mustChangePassword);
            internalUser.setDataScopeType(dataScopeType);
            return internalUser;
        } catch (RuntimeException exception) {
            logger.warn("Read internal user cache failed", exception);
            return null;
        }
    }

    /**
     * 写入内部账号登录快照。
     * 仅当关键字段完整时才写入，避免缓存脏数据。
     */
    @Override
    public void cacheInternalUserLoginSnapshot(InternalUserEntity internalUser) {
        if (!redisProperties.isEnabled() || hasInvalidInternalUserSnapshot(internalUser)) {
            return;
        }
        try {
            String value = String.join(
                    "|",
                    String.valueOf(internalUser.getId()),
                    internalUser.getPassword(),
                    String.valueOf(internalUser.getStatus()),
                    String.valueOf(internalUser.getMustChangePassword()),
                    String.valueOf(internalUser.getDataScopeType())
            );
            Duration ttl = Duration.ofSeconds(Math.max(redisProperties.getAuthorityCacheTtlSeconds(), 1L));
            stringRedisTemplate.opsForValue().set(buildInternalUserLoginKey(internalUser.getUsername()), value, ttl);
        } catch (RuntimeException exception) {
            logger.warn("Write internal user cache failed", exception);
        }
    }

    /**
     * 清理内部账号登录快照。
     */
    @Override
    public void clearInternalUserLoginSnapshot(String username) {
        if (!redisProperties.isEnabled() || username == null || username.isBlank()) {
            return;
        }
        try {
            stringRedisTemplate.delete(buildInternalUserLoginKey(username));
        } catch (RuntimeException exception) {
            logger.warn("Delete internal user cache failed", exception);
        }
    }

    /**
     * 读取当前登录失败次数；无记录时返回 0。
     */
    @Override
    public Integer getLoginFailCount(String username) {
        InternalLoginState loginState = readInternalLoginState(username);
        return loginState == null ? 0 : loginState.loginFailCount();
    }

    /**
     * 读取账号锁定截止时间戳（毫秒）；未锁定时返回 null。
     */
    @Override
    public Long getLockedUntil(String username) {
        InternalLoginState loginState = readInternalLoginState(username);
        return loginState == null ? null : loginState.lockedUntil();
    }

    /**
     * 记录一次登录失败并按阈值计算是否锁定账号。
     * 状态缓存结构：failCount|lockedUntilMillis(可空)
     */
    @Override
    public void recordLoginFailure(String username, int lockThreshold, long lockMinutes) {
        if (!redisProperties.isEnabled()
                || username == null
                || username.isBlank()
                || lockThreshold <= 0
                || lockMinutes <= 0L) {
            return;
        }
        try {
            long now = System.currentTimeMillis();
            InternalLoginState currentState = readInternalLoginState(username);
            int failCount = currentState == null ? 0 : currentState.loginFailCount();
            Long lockedUntil = currentState == null ? null : currentState.lockedUntil();
            if (lockedUntil != null && lockedUntil <= now) {
                failCount = 0;
                lockedUntil = null;
            }
            failCount = Math.min(failCount + 1, Math.max(redisProperties.getLoginFailCountMax(), 1));
            if (failCount >= Math.max(lockThreshold, 1)) {
                lockedUntil = now + Math.max(lockMinutes, 1L) * 60_000L;
            } else {
                lockedUntil = null;
            }
            Duration ttl = Duration.ofMinutes(Math.max(lockMinutes * 2L, Math.max(redisProperties.getLoginStateMinTtlMinutes(), 1L)));
            String value = failCount + "|" + (lockedUntil == null ? "" : lockedUntil);
            stringRedisTemplate.opsForValue().set(buildInternalUserLoginStateKey(username), value, ttl);
        } catch (RuntimeException exception) {
            logger.warn("Record internal login failure failed", exception);
        }
    }

    /**
     * 清理登录失败与锁定状态。
     */
    @Override
    public void clearLoginState(String username) {
        if (!redisProperties.isEnabled() || username == null || username.isBlank()) {
            return;
        }
        try {
            stringRedisTemplate.delete(buildInternalUserLoginStateKey(username));
        } catch (RuntimeException exception) {
            logger.warn("Clear internal login state failed", exception);
        }
    }

    /**
     * 构建内部账号登录快照缓存 Key。
     */
    private String buildInternalUserLoginKey(String username) {
        String normalizedUsername = username.trim().toLowerCase();
        return INTERNAL_USER_LOGIN_KEY_PREFIX + normalizedUsername;
    }

    /**
     * 构建内部账号登录状态缓存 Key。
     */
    private String buildInternalUserLoginStateKey(String username) {
        String normalizedUsername = username.trim().toLowerCase();
        return INTERNAL_USER_LOGIN_STATE_KEY_PREFIX + normalizedUsername;
    }

    /**
     * 读取并解析登录状态缓存。
     */
    private InternalLoginState readInternalLoginState(String username) {
        if (!redisProperties.isEnabled() || username == null || username.isBlank()) {
            return null;
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(buildInternalUserLoginStateKey(username));
            if (value == null || value.isBlank()) {
                return null;
            }
            String[] values = value.split("\\|", -1);
            if (values.length != 2) {
                return null;
            }
            int loginFailCount = Integer.parseInt(values[0]);
            Long lockedUntil = values[1].isBlank() ? null : Long.parseLong(values[1]);
            return new InternalLoginState(loginFailCount, lockedUntil);
        } catch (RuntimeException exception) {
            logger.warn("Read internal login state failed", exception);
            return null;
        }
    }

    /**
     * 读取并累计登录失败次数时使用的内部状态。
     * loginFailCount 表示连续失败次数，lockedUntil 表示锁定截止毫秒时间戳。
     */
    private record InternalLoginState(int loginFailCount, Long lockedUntil) {
    }

    /**
     * 校验登录快照是否具备写入缓存所需的最小字段。
     */
    private boolean hasInvalidInternalUserSnapshot(InternalUserEntity internalUser) {
        return internalUser == null
                || internalUser.getUsername() == null
                || internalUser.getUsername().isBlank()
                || internalUser.getId() == null
                || internalUser.getId() <= 0L
                || internalUser.getPassword() == null
                || internalUser.getPassword().isBlank()
                || internalUser.getStatus() == null
                || internalUser.getMustChangePassword() == null
                || internalUser.getDataScopeType() == null;
    }
}
