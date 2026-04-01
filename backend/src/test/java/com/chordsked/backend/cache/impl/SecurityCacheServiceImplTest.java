package com.chordsked.backend.cache.impl;

import com.chordsked.backend.config.properties.RedisProperties;
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
import static org.mockito.Mockito.mock;
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
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
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
}
