package com.chordsked.backend.utils.jwt;

import com.chordsked.backend.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component("jwtTokenUtils")
public class JwtTokenUtils {
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USER_TYPE = "userType";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";

    @Resource(name = "jwtProperties")
    private JwtProperties jwtProperties;

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, String expectedTokenType) {
        try {
            Claims claims = parseClaims(token);
            Long userId = claims.get(CLAIM_USER_ID, Long.class);
            String userType = claims.get(CLAIM_USER_TYPE, String.class);
            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            // 基础框架阶段仅校验通用 claim，后续可在这里扩展 jti、设备指纹、黑名单等安全策略。
            return userId != null && userType != null && !userType.isBlank() && expectedTokenType.equals(tokenType);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public String generateToken(Long userId, String userType, long expirationSeconds, String tokenType) {
        Instant now = Instant.now();
        return Jwts.builder()
                // subject 与 userId 对齐，便于后续统一用户服务按 userId 路由。
                .subject(String.valueOf(userId))
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_USER_TYPE, userType)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(signingKey())
                .compact();
    }

    private SecretKey signingKey() {
        byte[] secretBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(secretBytes);
    }
}
