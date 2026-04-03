package com.chordsked.backend.config.properties;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.stereotype.Component;

@Component("internalAuthLoginProperties")
@Validated
@ConfigurationProperties(prefix = "chordsked.security.internal-auth.login")
public class InternalAuthLoginProperties {
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
