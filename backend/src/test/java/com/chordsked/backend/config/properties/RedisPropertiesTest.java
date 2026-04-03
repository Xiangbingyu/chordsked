package com.chordsked.backend.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.env.PropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisPropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(RedisPropertiesTestConfiguration.class);

    @Test
    void shouldUseDefaultValuesWhenNoPropertyConfigured() {
        contextRunner.run(context -> {
            RedisProperties redisProperties = context.getBean(RedisProperties.class);
            assertTrue(redisProperties.isEnabled());
            assertEquals(300L, redisProperties.getAuthorityCacheTtlSeconds());
            assertEquals(300L, redisProperties.getUserSnapshotCacheTtlSeconds());
            assertTrue(redisProperties.isTokenSessionEnabled());
            assertEquals(255, redisProperties.getLoginFailCountMax());
            assertEquals(60L, redisProperties.getLoginStateMinTtlMinutes());
        });
    }

    @Test
    void shouldBindCanonicalSecurityRedisProperties() {
        contextRunner
                .withPropertyValues(
                        "chordsked.security.redis.enabled=false",
                        "chordsked.security.redis.authority-cache-ttl-seconds=120",
                        "chordsked.security.redis.user-snapshot-cache-ttl-seconds=90",
                        "chordsked.security.redis.token-session-enabled=false",
                        "chordsked.security.redis.login-fail-count-max=8",
                        "chordsked.security.redis.login-state-min-ttl-minutes=45"
                )
                .run(context -> {
                    RedisProperties redisProperties = context.getBean(RedisProperties.class);
                    assertFalse(redisProperties.isEnabled());
                    assertEquals(120L, redisProperties.getAuthorityCacheTtlSeconds());
                    assertEquals(90L, redisProperties.getUserSnapshotCacheTtlSeconds());
                    assertFalse(redisProperties.isTokenSessionEnabled());
                    assertEquals(8, redisProperties.getLoginFailCountMax());
                    assertEquals(45L, redisProperties.getLoginStateMinTtlMinutes());
                });
    }

    @Test
    void shouldKeepLegacyRedisPropertyCompatibilityThroughCommonConfig() {
        contextRunner
                .withInitializer(context -> loadCommonApplicationConfig(context.getEnvironment().getPropertySources()))
                .withPropertyValues(
                        "chordsked.redis.enabled=false",
                        "chordsked.redis.authority-cache-ttl-seconds=180",
                        "chordsked.redis.user-snapshot-cache-ttl-seconds=75",
                        "chordsked.redis.token-session-enabled=false",
                        "chordsked.redis.login-fail-count-max=6",
                        "chordsked.redis.login-state-min-ttl-minutes=20"
                )
                .run(context -> {
                    RedisProperties redisProperties = context.getBean(RedisProperties.class);
                    assertFalse(redisProperties.isEnabled());
                    assertEquals(180L, redisProperties.getAuthorityCacheTtlSeconds());
                    assertEquals(75L, redisProperties.getUserSnapshotCacheTtlSeconds());
                    assertFalse(redisProperties.isTokenSessionEnabled());
                    assertEquals(6, redisProperties.getLoginFailCountMax());
                    assertEquals(20L, redisProperties.getLoginStateMinTtlMinutes());
                });
    }

    private void loadCommonApplicationConfig(org.springframework.core.env.MutablePropertySources propertySources) {
        try {
            Resource resource = new ClassPathResource("common/application-common.yml");
            List<PropertySource<?>> propertySourcesFromYaml = new YamlPropertySourceLoader().load("application-common", resource);
            for (int index = propertySourcesFromYaml.size() - 1; index >= 0; index--) {
                propertySources.addLast(propertySourcesFromYaml.get(index));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Load common application config failed", exception);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(RedisProperties.class)
    static class RedisPropertiesTestConfiguration {
    }
}
