package com.chordsked.backend.utils.jwt;

import com.chordsked.backend.config.properties.JwtProperties;
import com.chordsked.backend.utils.normalize.StringNormalizeUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component("jwtTokenUtils")
public class JwtTokenUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtils.class);
    // 统一定义 tokenType，供鉴权链路和续签链路复用，避免硬编码字符串分散。
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    // 与文档约定一致的 JWT claim 名称。
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USER_TYPE = "userType";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    // 允许的账号类型白名单，防止非法 userType 混入鉴权链路。
    private static final Set<String> SUPPORTED_USER_TYPES = Arrays.stream(new String[]{"ADMIN", "TEACHER", "STUDENT"})
            .collect(Collectors.toUnmodifiableSet());

    @Resource(name = "jwtProperties")
    private JwtProperties jwtProperties;

    /**
     * 解析 JWT 并返回 Claims。
     * 解析失败时抛出 RuntimeException，由调用方决定降级或拦截策略。
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(buildSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims parseClaimsAllowExpired(String token) {
        try {
            return parseClaims(token);
        } catch (ExpiredJwtException exception) {
            return exception.getClaims();
        }
    }

    /**
     * 统一 token 有效性校验入口：
     * - userId 必须存在且大于 0
     * - userType 必须在白名单中
     * - tokenType 必须与调用方预期一致（access/refresh）
     * - issuer 必须匹配配置值
     */
    public boolean isTokenValid(String token, String expectedTokenType) {
        try {
            Claims claims = parseClaims(token);
            return isClaimsValid(claims, expectedTokenType);
        } catch (RuntimeException exception) {
            logger.warn("JWT token validation failed", exception);
            return false;
        }
    }

    public boolean isAccessTokenValid(String token) {
        // 业务接口鉴权只接受 access token。
        return isTokenValid(token, TOKEN_TYPE_ACCESS);
    }

    public boolean isRefreshTokenValid(String token) {
        // 续签接口校验 refresh token，避免 refresh/access 混用。
        return isTokenValid(token, TOKEN_TYPE_REFRESH);
    }

    /**
     * Claims 级别的校验方法。
     * 适用于调用方已经拿到 claims 的场景，例如过滤器中避免重复 parse。
     */
    public boolean isClaimsValid(Claims claims, String expectedTokenType) {
        Long userId = claims.get(CLAIM_USER_ID, Long.class);
        String userType = StringNormalizeUtils.normalizeOrEmpty(claims.get(CLAIM_USER_TYPE, String.class));
        String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
        String issuer = claims.getIssuer();
        Date expiration = claims.getExpiration();
        return userId != null
                && userId > 0
                && SUPPORTED_USER_TYPES.contains(userType)
                && expectedTokenType.equals(tokenType)
                && jwtProperties.getIssuer().equals(issuer)
                && expiration != null
                && expiration.after(new Date());
    }

    /**
     * 生成 JWT。
     * 调用方负责传入过期时间和 tokenType。
     * 如非特殊场景，优先使用 generateAccessToken 或 generateRefreshToken 以减少误用。
     */
    public String generateToken(Long userId, String userType, long expirationSeconds, String tokenType) {
        Instant now = Instant.now();
        String normalizedUserType = StringNormalizeUtils.normalizeOrEmpty(userType);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_USER_TYPE, normalizedUserType)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(buildSigningKey())
                .compact();
    }

    public String generateAccessToken(Long userId, String userType) {
        // access token 过期时间来自配置：chordsked.security.jwt.access-expiration-seconds
        return generateToken(userId, userType, jwtProperties.getAccessExpirationSeconds(), TOKEN_TYPE_ACCESS);
    }

    public String generateRefreshToken(Long userId, String userType) {
        // refresh token 过期时间来自配置：chordsked.security.jwt.refresh-expiration-seconds
        return generateToken(userId, userType, jwtProperties.getRefreshExpirationSeconds(), TOKEN_TYPE_REFRESH);
    }

    private SecretKey buildSigningKey() {
        // 基于配置密钥生成 HMAC key，密钥长度由 JwtProperties 在启动时校验。
        byte[] secretBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(secretBytes);
    }
}

