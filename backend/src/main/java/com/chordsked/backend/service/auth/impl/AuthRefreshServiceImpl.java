package com.chordsked.backend.service.auth.impl;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.config.properties.JwtProperties;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.vo.auth.AuthRefreshResultVO;
import com.chordsked.backend.service.auth.AuthRefreshService;
import com.chordsked.backend.service.verification.userstatus.UserStatusVerificationService;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service("authRefreshService")
public class AuthRefreshServiceImpl implements AuthRefreshService {
    private static final Logger logger = LoggerFactory.getLogger(AuthRefreshServiceImpl.class);

    @Resource(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @Resource(name = "jwtTokenUtils")
    private JwtTokenUtils jwtTokenUtils;

    @Resource(name = "jwtProperties")
    private JwtProperties jwtProperties;

    @Resource(name = "userStatusVerificationService")
    private UserStatusVerificationService userStatusVerificationService;

    /**
     * 执行 refresh token 续期流程。
     * 这里假定 controller 已经完成 refresh token claims 合法性、路由端一致性等入口层校验，
     * service 只负责补充编排入参校验与后续状态流转。
     */
    @Override
    public RefreshExecutionResult refreshToken(
            Long userId,
            AccountUserType userType,
            String accessToken,
            String refreshToken,
            boolean secureRequest
    ) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "刷新令牌无效，请重新登录");
        }
        if (userType == null || !StringUtils.hasText(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "刷新令牌无效，请重新登录");
        }
        /**
         * refresh token 必须仍处于 active 且未被 revoke，
         * 否则说明该登录态已经失效，不能继续续签。
         */
        if (!securityCacheService.isTokenActive(refreshToken) || securityCacheService.isTokenRevoked(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        /**
         * refresh 前再次校验用户状态。
         * 避免账号被禁用后，历史 refresh token 仍可继续续签。
         */
        userStatusVerificationService.verify(userId, userType);

        /**
         * 为当前用户签发全新的 access/refresh token。
         * 新 token 的 claims 会立即解析，用于后续写入缓存激活态。
         */
        String newAccessToken = jwtTokenUtils.generateAccessToken(userId, userType.getCode());
        String newRefreshToken = jwtTokenUtils.generateRefreshToken(userId, userType.getCode());
        Claims newAccessClaims = jwtTokenUtils.parseClaims(newAccessToken);
        Claims newRefreshClaims = jwtTokenUtils.parseClaims(newRefreshToken);

        /**
         * 旧 access/refresh token 在续签成功后立即吊销，
         * 防止旧 token 与新 token 并行长期有效。
         */
        revokeToken(accessToken);
        revokeToken(refreshToken);
        securityCacheService.markTokenActive(newAccessToken, newAccessClaims.getExpiration());
        securityCacheService.markTokenActive(newRefreshToken, newRefreshClaims.getExpiration());

        AuthRefreshResultVO result = new AuthRefreshResultVO();
        result.setUserId(userId);
        result.setUserType(userType);
        return new RefreshExecutionResult(
                result,
                newAccessToken,
                jwtProperties.getAccessExpirationSeconds(),
                newRefreshToken,
                jwtProperties.getRefreshExpirationSeconds(),
                secureRequest
        );
    }

    /**
     * 吊销旧 token。
     * 如果 token 为空、已损坏或 claims 无法解析，则跳过吊销并记录日志，
     * 避免因此阻断主 refresh 流程。
     */
    private void revokeToken(String token) {
        if (token == null) {
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
