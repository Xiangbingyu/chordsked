package com.chordsked.backend.cache.auth.provider;

import com.chordsked.backend.config.properties.RedisProperties;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.auth.UsernamePasswordLoginSnapshot;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.UserDataScopeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InternalAuthSnapshotCacheProviderTest {
    private UsernamePasswordLoginSnapshotCacheProvider usernamePasswordLoginSnapshotCacheProvider;
    private RedisProperties redisProperties;
    private StringRedisTemplate stringRedisTemplate;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        usernamePasswordLoginSnapshotCacheProvider = new UsernamePasswordLoginSnapshotCacheProvider();
        redisProperties = mock(RedisProperties.class);
        stringRedisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);

        ReflectionTestUtils.setField(usernamePasswordLoginSnapshotCacheProvider, "redisProperties", redisProperties);
        ReflectionTestUtils.setField(usernamePasswordLoginSnapshotCacheProvider, "stringRedisTemplate", stringRedisTemplate);

        when(redisProperties.isEnabled()).thenReturn(true);
        when(redisProperties.getUserSnapshotCacheTtlSeconds()).thenReturn(300L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldSkipSnapshotCacheWhenInternalUserInvalid() {
        UsernamePasswordLoginSnapshot loginSnapshot = new UsernamePasswordLoginSnapshot();
        loginSnapshot.setUserType(AccountUserType.ADMIN);
        loginSnapshot.setLoginMethod(AuthLoginMethod.USERNAME_PASSWORD);
        loginSnapshot.setUserId(0L);
        loginSnapshot.setPrincipal("admin");
        loginSnapshot.setPassword(" ");

        usernamePasswordLoginSnapshotCacheProvider.cacheLoginSnapshot(loginSnapshot);

        verify(stringRedisTemplate, never()).opsForValue();
    }

    @Test
    void shouldCacheSnapshotWithUserSnapshotTtl() {
        UsernamePasswordLoginSnapshot loginSnapshot = new UsernamePasswordLoginSnapshot();
        loginSnapshot.setUserId(1001L);
        loginSnapshot.setUserType(AccountUserType.ADMIN);
        loginSnapshot.setLoginMethod(AuthLoginMethod.USERNAME_PASSWORD);
        loginSnapshot.setPrincipal("Admin");
        loginSnapshot.setName("管理员");
        loginSnapshot.setEnabled(true);
        loginSnapshot.setMustChangePassword(false);
        loginSnapshot.setPassword("encodedPassword");
        loginSnapshot.setPhone("13800138000");
        loginSnapshot.setDataScopeType(UserDataScopeType.ASSIGNED);
        when(redisProperties.getUserSnapshotCacheTtlSeconds()).thenReturn(123L);

        usernamePasswordLoginSnapshotCacheProvider.cacheLoginSnapshot(loginSnapshot);

        verify(valueOperations).set(
                "chordsked:auth:admin:username-password:admin",
                "1001|管理员|true|false|encodedPassword|13800138000|2",
                Duration.ofSeconds(123L)
        );
    }

    @Test
    void shouldReturnSnapshotWhenCacheHit() {
        when(valueOperations.get("chordsked:auth:admin:username-password:admin"))
                .thenReturn("1001|管理员|true|false|encodedPassword|13800138000|2");

        UsernamePasswordLoginSnapshot snapshot = (UsernamePasswordLoginSnapshot) usernamePasswordLoginSnapshotCacheProvider
                .getLoginSnapshot(AccountUserType.ADMIN, "admin");

        assertNotNull(snapshot);
        assertEquals(1001L, snapshot.getUserId());
        assertEquals(AccountUserType.ADMIN, snapshot.getUserType());
        assertEquals(AuthLoginMethod.USERNAME_PASSWORD, snapshot.getLoginMethod());
        assertEquals("admin", snapshot.getPrincipal());
        assertEquals("管理员", snapshot.getName());
        assertTrue(snapshot.getEnabled());
        assertFalse(snapshot.getMustChangePassword());
        assertEquals("encodedPassword", snapshot.getPassword());
        assertEquals("13800138000", snapshot.getPhone());
        assertEquals(UserDataScopeType.ASSIGNED, snapshot.getDataScopeType());
    }

    @Test
    void shouldReadOldSnapshotPayloadWithoutDataScopeType() {
        when(valueOperations.get("chordsked:auth:admin:username-password:admin"))
                .thenReturn("1001|管理员|true|false|encodedPassword|13800138000");

        UsernamePasswordLoginSnapshot snapshot = (UsernamePasswordLoginSnapshot) usernamePasswordLoginSnapshotCacheProvider
                .getLoginSnapshot(AccountUserType.ADMIN, "admin");

        assertNotNull(snapshot);
        assertNull(snapshot.getDataScopeType());
    }

    @Test
    void shouldReturnNullWhenSnapshotPayloadMalformed() {
        when(valueOperations.get("chordsked:auth:admin:username-password:admin"))
                .thenReturn("invalid-payload");

        UsernamePasswordLoginSnapshot snapshot = (UsernamePasswordLoginSnapshot) usernamePasswordLoginSnapshotCacheProvider
                .getLoginSnapshot(AccountUserType.ADMIN, "admin");

        assertNull(snapshot);
    }

    @Test
    void shouldClearSnapshotCache() {
        usernamePasswordLoginSnapshotCacheProvider.clearLoginSnapshot(AccountUserType.ADMIN, "Admin");

        verify(stringRedisTemplate).delete("chordsked:auth:admin:username-password:admin");
    }
}
