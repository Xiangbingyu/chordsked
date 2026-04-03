package com.chordsked.backend.cache.impl;

import com.chordsked.backend.config.properties.RedisProperties;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.enums.InternalUserDataScopeType;
import com.chordsked.backend.model.enums.InternalUserStatus;
import com.chordsked.backend.model.enums.MustChangePasswordFlag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InternalAuthLoginCacheServiceImplTest {
    private InternalAuthLoginCacheServiceImpl internalAuthLoginCacheService;
    private RedisProperties redisProperties;
    private StringRedisTemplate stringRedisTemplate;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        internalAuthLoginCacheService = new InternalAuthLoginCacheServiceImpl();
        redisProperties = mock(RedisProperties.class);
        stringRedisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);

        ReflectionTestUtils.setField(internalAuthLoginCacheService, "redisProperties", redisProperties);
        ReflectionTestUtils.setField(internalAuthLoginCacheService, "stringRedisTemplate", stringRedisTemplate);

        when(redisProperties.isEnabled()).thenReturn(true);
        when(redisProperties.getAuthorityCacheTtlSeconds()).thenReturn(300L);
        when(redisProperties.getLoginFailCountMax()).thenReturn(255);
        when(redisProperties.getLoginStateMinTtlMinutes()).thenReturn(60L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldSkipSnapshotCacheWhenInternalUserInvalid() {
        InternalUserEntity internalUser = new InternalUserEntity();
        internalUser.setId(0L);
        internalUser.setUsername("admin");
        internalUser.setPassword(" ");
        internalUser.setStatusEnum(InternalUserStatus.ENABLED);
        internalUser.setMustChangePasswordEnum(MustChangePasswordFlag.NO);
        internalUser.setDataScopeTypeEnum(InternalUserDataScopeType.ALL_COMPANY);

        internalAuthLoginCacheService.cacheInternalUserLoginSnapshot(internalUser);

        verify(stringRedisTemplate, never()).opsForValue();
    }

    @Test
    void shouldSkipRecordLoginFailureWhenThresholdOrMinutesInvalid() {
        clearInvocations(stringRedisTemplate, valueOperations);

        internalAuthLoginCacheService.recordLoginFailure("admin", 0, 30L);
        internalAuthLoginCacheService.recordLoginFailure("admin", 5, 0L);

        verify(stringRedisTemplate, never()).opsForValue();
    }
}
