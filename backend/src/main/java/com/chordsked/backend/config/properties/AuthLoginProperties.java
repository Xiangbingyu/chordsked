package com.chordsked.backend.config.properties;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component("authLoginProperties")
@Validated
@ConfigurationProperties(prefix = "chordsked.security.auth.login")
public class AuthLoginProperties {
    @Min(1)
    private int failLockThreshold = 5;
    @Min(1)
    private long failLockMinutes = 30L;

    public int getFailLockThreshold() {
        return failLockThreshold;
    }

    public void setFailLockThreshold(int failLockThreshold) {
        this.failLockThreshold = failLockThreshold;
    }

    public long getFailLockMinutes() {
        return failLockMinutes;
    }

    public void setFailLockMinutes(long failLockMinutes) {
        this.failLockMinutes = failLockMinutes;
    }
}
