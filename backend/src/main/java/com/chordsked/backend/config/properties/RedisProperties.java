package com.chordsked.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "chordsked.security.redis")
@Component("redisProperties")
public class RedisProperties {
    private boolean enabled = true;
    private long authorityCacheTtlSeconds = 300L;
    private long userSnapshotCacheTtlSeconds = 300L;
    private boolean tokenSessionEnabled = true;
    private int loginFailCountMax = 255;
    private long loginStateMinTtlMinutes = 60L;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getAuthorityCacheTtlSeconds() {
        return authorityCacheTtlSeconds;
    }

    public void setAuthorityCacheTtlSeconds(long authorityCacheTtlSeconds) {
        this.authorityCacheTtlSeconds = authorityCacheTtlSeconds;
    }

    public boolean isTokenSessionEnabled() {
        return tokenSessionEnabled;
    }

    public long getUserSnapshotCacheTtlSeconds() {
        return userSnapshotCacheTtlSeconds;
    }

    public void setUserSnapshotCacheTtlSeconds(long userSnapshotCacheTtlSeconds) {
        this.userSnapshotCacheTtlSeconds = userSnapshotCacheTtlSeconds;
    }

    public void setTokenSessionEnabled(boolean tokenSessionEnabled) {
        this.tokenSessionEnabled = tokenSessionEnabled;
    }

    public int getLoginFailCountMax() {
        return loginFailCountMax;
    }

    public void setLoginFailCountMax(int loginFailCountMax) {
        this.loginFailCountMax = loginFailCountMax;
    }

    public long getLoginStateMinTtlMinutes() {
        return loginStateMinTtlMinutes;
    }

    public void setLoginStateMinTtlMinutes(long loginStateMinTtlMinutes) {
        this.loginStateMinTtlMinutes = loginStateMinTtlMinutes;
    }

}
