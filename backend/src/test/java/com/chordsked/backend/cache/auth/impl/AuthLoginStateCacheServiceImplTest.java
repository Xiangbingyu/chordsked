package com.chordsked.backend.cache.auth.impl;

import com.chordsked.backend.config.properties.RedisProperties;
import com.chordsked.backend.model.enums.AccountUserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthLoginStateCacheServiceImplTest {
    private AuthLoginStateCacheServiceImpl authLoginStateCacheService;
    private RedisProperties redisProperties;
    private StringRedisTemplate stringRedisTemplate;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        authLoginStateCacheService = new AuthLoginStateCacheServiceImpl();
        redisProperties = mock(RedisProperties.class);
        stringRedisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);

        ReflectionTestUtils.setField(authLoginStateCacheService, "redisProperties", redisProperties);
        ReflectionTestUtils.setField(authLoginStateCacheService, "stringRedisTemplate", stringRedisTemplate);

        when(redisProperties.isEnabled()).thenReturn(true);
        when(redisProperties.getLoginFailCountMax()).thenReturn(255);
        when(redisProperties.getLoginStateMinTtlMinutes()).thenReturn(60L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldSkipRecordLoginFailureWhenThresholdOrMinutesInvalid() {
        clearInvocations(stringRedisTemplate, valueOperations);

        authLoginStateCacheService.recordLoginFailure(AccountUserType.ADMIN, "admin", 0, 30L);
        authLoginStateCacheService.recordLoginFailure(AccountUserType.ADMIN, "admin", 5, 0L);

        verify(stringRedisTemplate, never()).opsForValue();
    }

    @Test
    void shouldSkipRecordLoginFailureWhenUserTypeMissing() {
        clearInvocations(stringRedisTemplate, valueOperations);

        authLoginStateCacheService.recordLoginFailure(null, "admin", 5, 30L);

        verify(stringRedisTemplate, never()).opsForValue();
    }

    @Test
    void shouldRecordLoginFailureWithConfiguredTtl() {
        when(valueOperations.get("chordsked:auth:login:state:admin:admin")).thenReturn(null);

        authLoginStateCacheService.recordLoginFailure(AccountUserType.ADMIN, "Admin", 5, 30L);

        verify(valueOperations).set(
                "chordsked:auth:login:state:admin:admin",
                "1|",
                Duration.ofMinutes(60L)
        );
    }

    @Test
    void shouldReadLoginStateWhenCacheHit() {
        when(valueOperations.get("chordsked:auth:login:state:admin:admin")).thenReturn("3|1710000000000");

        Integer failCount = authLoginStateCacheService.getLoginFailCount(AccountUserType.ADMIN, "admin");
        Long lockedUntil = authLoginStateCacheService.getLockedUntil(AccountUserType.ADMIN, "admin");

        assertEquals(3, failCount);
        assertEquals(1710000000000L, lockedUntil);
    }

    @Test
    void shouldReturnDefaultStateWhenPayloadMalformed() {
        when(valueOperations.get("chordsked:auth:login:state:admin:admin")).thenReturn("invalid");

        Integer failCount = authLoginStateCacheService.getLoginFailCount(AccountUserType.ADMIN, "admin");
        Long lockedUntil = authLoginStateCacheService.getLockedUntil(AccountUserType.ADMIN, "admin");

        assertEquals(0, failCount);
        assertNull(lockedUntil);
    }

    @Test
    void shouldClearLoginState() {
        authLoginStateCacheService.clearLoginState(AccountUserType.ADMIN, "Admin");

        verify(stringRedisTemplate).delete("chordsked:auth:login:state:admin:admin");
    }
}
