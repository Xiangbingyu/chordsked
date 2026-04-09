package com.chordsked.backend.service.auth.impl;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.service.auth.AuthLogoutService;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 退出登录服务。
 * 负责把 access token 与 refresh token 标记为 revoked，
 * 具体 Cookie 读取与清理由 controller 层处理，保持 service 不感知 Servlet 细节。
 */
@Service("authLogoutService")
public class AuthLogoutServiceImpl implements AuthLogoutService {
    private static final Logger logger = LoggerFactory.getLogger(AuthLogoutServiceImpl.class);

    @Resource(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @Resource(name = "jwtTokenUtils")
    private JwtTokenUtils jwtTokenUtils;

    @Override
    /**
     * 按“尽力而为”原则处理登出。
     * 两类 token 会分别尝试撤销，任意一个 token 解析失败都不会影响另一个 token 的撤销动作。
     */
    public void logout(String accessToken, String refreshToken) {
        revokeToken(accessToken);
        revokeToken(refreshToken);
    }

    /**
     * 将单个 token 标记为 revoked。
     * 这里允许使用已过期 token 的 claims，只要还能解析出 expiration，就仍然可以把剩余生命周期内的缓存状态改为撤销。
     */
    private void revokeToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            Claims claims = jwtTokenUtils.parseClaimsAllowExpired(token);
            Date expiration = claims == null ? null : claims.getExpiration();
            if (expiration == null) {
                return;
            }
            securityCacheService.markTokenRevoked(token, expiration);
        } catch (RuntimeException exception) {
            logger.warn("Token revoke skipped because token parsing failed", exception);
        }
    }
}
