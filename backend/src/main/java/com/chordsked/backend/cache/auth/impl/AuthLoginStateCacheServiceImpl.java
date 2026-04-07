package com.chordsked.backend.cache.auth.impl;

import com.chordsked.backend.cache.auth.AuthLoginStateCacheService;
import com.chordsked.backend.config.properties.RedisProperties;
import com.chordsked.backend.model.enums.AccountUserType;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;

/**
 * 统一维护登录失败次数与锁定时间的 Redis 缓存。
 * 该实现只处理短期登录状态，不承载用户详情快照。
 * 缓存 Key 按账号类型与登录标识组合，避免不同端之间互相污染状态。
 */
@Component("authLoginStateCacheService")
public class AuthLoginStateCacheServiceImpl implements AuthLoginStateCacheService {
    private static final Logger logger = LoggerFactory.getLogger(AuthLoginStateCacheServiceImpl.class);
    private static final String LOGIN_STATE_KEY_PREFIX = "chordsked:auth:login:state:";

    @Resource(name = "redisProperties")
    private RedisProperties redisProperties;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 读取当前账号的连续失败次数。
     * 当缓存不存在或不可用时返回 0，调用方可按“未失败”处理。
     */
    @Override
    public Integer getLoginFailCount(AccountUserType userType, String principal) {
        LoginState loginState = loadLoginState(userType, principal);
        return loginState == null ? 0 : loginState.loginFailCount();
    }

    /**
     * 读取当前账号被锁定到的时间点。
     * 返回 null 表示当前没有锁定信息。
     */
    @Override
    public Long getLockedUntil(AccountUserType userType, String principal) {
        LoginState loginState = loadLoginState(userType, principal);
        return loginState == null ? null : loginState.lockedUntil();
    }

    /**
     * 记录一次登录失败。
     * 当失败次数达到阈值后，会写入锁定截止时间，并为整条状态设置 TTL。
     */
    @Override
    public void recordLoginFailure(AccountUserType userType, String principal, int lockThreshold, long lockMinutes) {
        if (!redisProperties.isEnabled()
                || userType == null
                || principal == null
                || principal.isBlank()
                || lockThreshold <= 0
                || lockMinutes <= 0L) {
            return;
        }
        try {
            long now = System.currentTimeMillis();
            LoginState currentState = loadLoginState(userType, principal);
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
            stringRedisTemplate.opsForValue().set(buildLoginStateKey(userType, principal), value, ttl);
        } catch (RuntimeException exception) {
            logger.warn("Record login failure failed, userType={}", userType, exception);
        }
    }

    /**
     * 登录成功后清理失败次数与锁定状态。
     */
    @Override
    public void clearLoginState(AccountUserType userType, String principal) {
        if (!redisProperties.isEnabled()
                || userType == null
                || principal == null
                || principal.isBlank()) {
            return;
        }
        try {
            stringRedisTemplate.delete(buildLoginStateKey(userType, principal));
        } catch (RuntimeException exception) {
            logger.warn("Clear login state failed, userType={}", userType, exception);
        }
    }

    /**
     * 从 Redis 装载登录状态。
     * 状态值采用“失败次数|锁定截止时间”的紧凑格式存储，便于快速读写。
     */
    private LoginState loadLoginState(AccountUserType userType, String principal) {
        if (!redisProperties.isEnabled()
                || userType == null
                || principal == null
                || principal.isBlank()) {
            return null;
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(buildLoginStateKey(userType, principal));
            if (value == null || value.isBlank()) {
                return null;
            }
            String[] values = value.split("\\|", -1);
            if (values.length != 2) {
                return null;
            }
            int loginFailCount = Integer.parseInt(values[0]);
            Long lockedUntil = values[1].isBlank() ? null : Long.parseLong(values[1]);
            return new LoginState(loginFailCount, lockedUntil);
        } catch (RuntimeException exception) {
            logger.warn("Read login state failed, userType={}", userType, exception);
            return null;
        }
    }

    /**
     * 构建登录状态缓存 Key。
     * principal 统一转小写，减少同一账号大小写差异带来的重复缓存。
     */
    private String buildLoginStateKey(AccountUserType userType, String principal) {
        return LOGIN_STATE_KEY_PREFIX
                + userType.getCode().toLowerCase(Locale.ROOT)
                + ":"
                + principal.trim().toLowerCase(Locale.ROOT);
    }

    private record LoginState(int loginFailCount, Long lockedUntil) {
    }
}
