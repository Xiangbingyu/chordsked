package com.chordsked.backend.cache.auth.provider;

import com.chordsked.backend.config.properties.RedisProperties;
import com.chordsked.backend.model.auth.AuthLoginSnapshot;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.auth.UsernamePasswordLoginSnapshot;
import com.chordsked.backend.model.enums.AccountUserType;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;

/**
 * 账号密码登录快照缓存 provider。
 * 负责将 UsernamePasswordLoginSnapshot 按 userType 与 principal 写入 Redis，
 * 以便 support 层在装载快照时优先命中缓存，减少对账号表的重复查询。
 */
@Component("usernamePasswordLoginSnapshotCacheProvider")
public class UsernamePasswordLoginSnapshotCacheProvider implements AuthLoginSnapshotCacheProvider {
    private static final Logger logger = LoggerFactory.getLogger(UsernamePasswordLoginSnapshotCacheProvider.class);
    private static final String CACHE_KEY_PREFIX = "chordsked:auth:";
    private static final String CACHE_KEY_SUFFIX = ":username-password:";

    @Resource(name = "redisProperties")
    private RedisProperties redisProperties;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public AuthLoginMethod getLoginMethod() {
        return AuthLoginMethod.USERNAME_PASSWORD;
    }

    @Override
    /**
     * 读取账号密码登录快照缓存。
     * 当前缓存值使用定长字符串序列化，读取失败或格式不合法时直接按未命中处理。
     */
    public AuthLoginSnapshot getLoginSnapshot(AccountUserType userType, String principal) {
        if (!redisProperties.isEnabled() || userType == null || principal == null || principal.isBlank()) {
            return null;
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(buildCacheKey(userType, principal));
            if (value == null || value.isBlank()) {
                return null;
            }
            String[] values = value.split("\\|", -1);
            if (values.length != 6) {
                return null;
            }
            UsernamePasswordLoginSnapshot snapshot = new UsernamePasswordLoginSnapshot();
            snapshot.setUserId(Long.parseLong(values[0]));
            snapshot.setUserType(userType);
            snapshot.setLoginMethod(AuthLoginMethod.USERNAME_PASSWORD);
            snapshot.setPrincipal(principal.trim());
            snapshot.setName(emptyToNull(values[1]));
            snapshot.setEnabled(Boolean.parseBoolean(values[2]));
            snapshot.setMustChangePassword(Boolean.parseBoolean(values[3]));
            snapshot.setPassword(values[4]);
            snapshot.setPhone(emptyToNull(values[5]));
            return snapshot;
        } catch (RuntimeException exception) {
            logger.warn("Read username password login snapshot failed", exception);
            return null;
        }
    }

    @Override
    /**
     * 写入账号密码登录快照缓存。
     * 只有在 Redis 可用且快照数据完整时才会写入，避免污染缓存。
     */
    public void cacheLoginSnapshot(AuthLoginSnapshot snapshot) {
        if (!redisProperties.isEnabled() || !(snapshot instanceof UsernamePasswordLoginSnapshot usernamePasswordSnapshot)
                || hasInvalidSnapshot(usernamePasswordSnapshot)) {
            return;
        }
        try {
            String value = String.join(
                    "|",
                    String.valueOf(usernamePasswordSnapshot.getUserId()),
                    usernamePasswordSnapshot.getName() == null ? "" : usernamePasswordSnapshot.getName(),
                    String.valueOf(Boolean.TRUE.equals(usernamePasswordSnapshot.getEnabled())),
                    String.valueOf(Boolean.TRUE.equals(usernamePasswordSnapshot.getMustChangePassword())),
                    usernamePasswordSnapshot.getPassword(),
                    usernamePasswordSnapshot.getPhone() == null ? "" : usernamePasswordSnapshot.getPhone()
            );
            Duration ttl = Duration.ofSeconds(Math.max(redisProperties.getUserSnapshotCacheTtlSeconds(), 1L));
            stringRedisTemplate.opsForValue().set(
                    buildCacheKey(usernamePasswordSnapshot.getUserType(), usernamePasswordSnapshot.getPrincipal()),
                    value,
                    ttl
            );
        } catch (RuntimeException exception) {
            logger.warn("Write username password login snapshot failed", exception);
        }
    }

    @Override
    /**
     * 删除指定 userType 与 principal 下的账号密码登录快照缓存。
     * 常用于密码更新、账号状态变更等需要强制失效快照的场景。
     */
    public void clearLoginSnapshot(AccountUserType userType, String principal) {
        if (!redisProperties.isEnabled() || userType == null || principal == null || principal.isBlank()) {
            return;
        }
        try {
            stringRedisTemplate.delete(buildCacheKey(userType, principal));
        } catch (RuntimeException exception) {
            logger.warn("Delete username password login snapshot failed", exception);
        }
    }

    /**
     * 构建账号密码登录快照缓存 key。
     * 当前只承担缓存职责，因此直接使用 userType code 参与 key 构建，不再承载业务支持矩阵判断。
     */
    private String buildCacheKey(AccountUserType userType, String principal) {
        String normalizedPrincipal = principal.trim().toLowerCase(Locale.ROOT);
        return CACHE_KEY_PREFIX + userType.getCode().toLowerCase(Locale.ROOT) + CACHE_KEY_SUFFIX + normalizedPrincipal;
    }

    /**
     * 校验待写入缓存的快照是否完整且与当前 provider 语义一致。
     */
    private boolean hasInvalidSnapshot(UsernamePasswordLoginSnapshot snapshot) {
        return snapshot == null
                || snapshot.getLoginMethod() == null
                || !AuthLoginMethod.USERNAME_PASSWORD.equals(snapshot.getLoginMethod())
                || snapshot.getUserType() == null
                || snapshot.getUserId() == null
                || snapshot.getUserId() <= 0L
                || snapshot.getPrincipal() == null
                || snapshot.getPrincipal().isBlank()
                || snapshot.getPassword() == null
                || snapshot.getPassword().isBlank();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
