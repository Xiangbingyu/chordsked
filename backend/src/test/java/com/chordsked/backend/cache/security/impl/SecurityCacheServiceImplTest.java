package com.chordsked.backend.cache.security.impl;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.config.properties.RedisProperties;
import com.chordsked.backend.model.enums.UserDataScopeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityCacheServiceImplTest {
    private SecurityCacheServiceImpl securityCacheService;
    private RedisProperties redisProperties;
    private StringRedisTemplate stringRedisTemplate;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        securityCacheService = new SecurityCacheServiceImpl();
        redisProperties = mock(RedisProperties.class);
        stringRedisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);

        ReflectionTestUtils.setField(securityCacheService, "redisProperties", redisProperties);
        ReflectionTestUtils.setField(securityCacheService, "stringRedisTemplate", stringRedisTemplate);

        when(redisProperties.isEnabled()).thenReturn(true);
        when(redisProperties.getAuthorityCacheTtlSeconds()).thenReturn(300L);
        when(redisProperties.getUserSnapshotCacheTtlSeconds()).thenReturn(300L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldReadUserSnapshotFromCache() {
        when(valueOperations.get("chordsked:security:user:ADMIN:1001")).thenReturn("1|2001|11|2");

        SecurityCacheService.SecurityUserSnapshot userSnapshot = securityCacheService.getUserSnapshot("ADMIN", 1001L);

        assertEquals("ADMIN", userSnapshot.userType());
        assertEquals(1001L, userSnapshot.userId());
        assertEquals(true, userSnapshot.enabled());
        assertEquals(2001L, userSnapshot.currentCampusId());
        assertEquals(11L, userSnapshot.primaryOrgNodeId());
        assertEquals(UserDataScopeType.ASSIGNED, userSnapshot.dataScopeType());
    }

    @Test
    void shouldReadLegacyUserSnapshotWithoutDataScopeType() {
        when(valueOperations.get("chordsked:security:user:ADMIN:1001")).thenReturn("1|2001");

        SecurityCacheService.SecurityUserSnapshot userSnapshot = securityCacheService.getUserSnapshot("ADMIN", 1001L);

        assertEquals("ADMIN", userSnapshot.userType());
        assertEquals(1001L, userSnapshot.userId());
        assertEquals(true, userSnapshot.enabled());
        assertEquals(2001L, userSnapshot.currentCampusId());
        assertEquals(null, userSnapshot.dataScopeType());
    }

    @Test
    void shouldReadExtendedUserSnapshotByUsingFirstThreeFields() {
        when(valueOperations.get("chordsked:security:user:ADMIN:1001")).thenReturn("1|2001|11|2");

        SecurityCacheService.SecurityUserSnapshot userSnapshot = securityCacheService.getUserSnapshot("ADMIN", 1001L);

        assertEquals("ADMIN", userSnapshot.userType());
        assertEquals(1001L, userSnapshot.userId());
        assertEquals(true, userSnapshot.enabled());
        assertEquals(2001L, userSnapshot.currentCampusId());
        assertEquals(11L, userSnapshot.primaryOrgNodeId());
        assertEquals(UserDataScopeType.ASSIGNED, userSnapshot.dataScopeType());
    }

    @Test
    void shouldWriteUserSnapshotToCache() {
        securityCacheService.cacheUserSnapshot(
                new SecurityCacheService.SecurityUserSnapshot(
                        "ADMIN",
                        1001L,
                        true,
                        2001L,
                        11L,
                        UserDataScopeType.ASSIGNED
                )
        );

        verify(valueOperations).set(
                eq("chordsked:security:user:ADMIN:1001"),
                eq("1|2001|11|2"),
                eq(Duration.ofSeconds(300))
        );
    }

    @Test
    void shouldEvictUserSnapshotCache() {
        securityCacheService.clearUserSnapshot("ADMIN", 1001L);

        verify(stringRedisTemplate).delete("chordsked:security:user:ADMIN:1001");
    }

    @Test
    void shouldReturnEmptyListWhenAuthorityNegativeCacheExists() {
        when(valueOperations.get("chordsked:security:authority:ADMIN:1001")).thenReturn("__EMPTY__");

        List<String> codes = securityCacheService.getAuthorityCodes("ADMIN", 1001L);

        assertEquals(Collections.emptyList(), codes);
    }

    @Test
    void shouldWriteNegativeCacheWhenAuthorityCodesEmpty() {
        securityCacheService.cacheAuthorityCodes("ADMIN", 1001L, Collections.emptyList());

        verify(valueOperations).set(
                eq("chordsked:security:authority:ADMIN:1001"),
                eq("__EMPTY__"),
                eq(Duration.ofSeconds(300))
        );
    }

    @Test
    void shouldEvictAuthorityCache() {
        securityCacheService.clearAuthorityCodes("ADMIN", 1001L);

        verify(stringRedisTemplate).delete("chordsked:security:authority:ADMIN:1001");
    }

    @Test
    void shouldReturnEmptyListWhenAuthorityCacheParameterInvalid() {
        List<String> codes = securityCacheService.getAuthorityCodes("   ", 1001L);

        assertEquals(Collections.emptyList(), codes);
        verify(stringRedisTemplate, never()).opsForValue();
    }

    @Test
    void shouldSkipWriteWhenAuthorityCacheParameterInvalid() {
        clearInvocations(stringRedisTemplate, valueOperations);

        securityCacheService.cacheAuthorityCodes("ADMIN", 0L, Collections.emptyList());

        verify(stringRedisTemplate, never()).opsForValue();
    }
}
