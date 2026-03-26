package com.chordsked.backend.config.properties;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component("jwtProperties")
@ConfigurationProperties(prefix = "chordsked.security.jwt")
public class JwtProperties {
    private String secret;
    private String issuer = "chordsked-backend";
    private long accessExpirationSeconds = 7200L;
    private long refreshExpirationSeconds = 1209600L;

    @PostConstruct
    public void validate() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret must be configured");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT secret length must be at least 32 characters");
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }

    public void setAccessExpirationSeconds(long accessExpirationSeconds) {
        this.accessExpirationSeconds = accessExpirationSeconds;
    }

    public long getRefreshExpirationSeconds() {
        return refreshExpirationSeconds;
    }

    public void setRefreshExpirationSeconds(long refreshExpirationSeconds) {
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }
}
