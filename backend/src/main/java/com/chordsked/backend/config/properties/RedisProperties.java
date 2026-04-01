package com.chordsked.backend.config.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("redisProperties")
public class RedisProperties {
    @Value("${chordsked.redis.enabled:${chordsked.security.redis.enabled:true}}")
    private boolean enabled;
    @Value("${chordsked.redis.authority-cache-ttl-seconds:${chordsked.redis.authorityCacheTtlSeconds:${chordsked.security.redis.authority-cache-ttl-seconds:${chordsked.security.redis.authorityCacheTtlSeconds:300}}}}")
    private long authorityCacheTtlSeconds;
    @Value("${chordsked.redis.token-session-enabled:${chordsked.redis.tokenSessionEnabled:${chordsked.security.redis.token-session-enabled:${chordsked.security.redis.tokenSessionEnabled:true}}}}")
    private boolean tokenSessionEnabled;

    public boolean isEnabled() {
        return enabled;
    }

    public long getAuthorityCacheTtlSeconds() {
        return authorityCacheTtlSeconds;
    }

    public boolean isTokenSessionEnabled() {
        return tokenSessionEnabled;
    }

}
