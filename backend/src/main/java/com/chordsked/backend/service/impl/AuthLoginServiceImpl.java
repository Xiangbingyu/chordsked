package com.chordsked.backend.service.impl;

import com.chordsked.backend.cache.AuthLoginCacheService;
import com.chordsked.backend.cache.SecurityCacheService;
import com.chordsked.backend.config.properties.JwtProperties;
import com.chordsked.backend.dao.mapper.InternalLoginMapper;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.AuthLoginRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.enums.InternalUserStatus;
import com.chordsked.backend.model.enums.MustChangePasswordFlag;
import com.chordsked.backend.model.vo.AuthLoginResultVO;
import com.chordsked.backend.service.AuthLoginService;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service("authLoginService")
public class AuthLoginServiceImpl implements AuthLoginService {
    private static final String USER_TYPE = "ADMIN";
    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @Resource(name = "internalLoginMapper")
    private InternalLoginMapper internalLoginMapper;

    @Resource(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @Resource(name = "authLoginCacheService")
    private AuthLoginCacheService authLoginCacheService;

    @Resource(name = "jwtTokenUtils")
    private JwtTokenUtils jwtTokenUtils;

    @Resource(name = "jwtProperties")
    private JwtProperties jwtProperties;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthLoginResultVO login(
            AuthLoginRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        String username = request.getUsername().trim();
        InternalUserEntity internalUser = loadInternalUserForLogin(username);
        if (internalUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (!InternalUserStatus.ENABLED.equals(internalUser.getStatusEnum())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号已被禁用，请联系管理员");
        }
        if (internalUser.getLockedUntil() != null && internalUser.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号已锁定，请 30 分钟后重试");
        }
        if (!passwordEncoder.matches(request.getPassword(), internalUser.getPassword())) {
            LocalDateTime now = LocalDateTime.now();
            internalLoginMapper.increaseInternalUserLoginFail(internalUser.getId(), now.plusMinutes(30), now);
            authLoginCacheService.evictInternalUserLoginSnapshot(username);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }

        LocalDateTime now = LocalDateTime.now();
        internalLoginMapper.resetInternalUserLoginFail(internalUser.getId(), now);
        internalUser.setLoginFailCount(0);
        internalUser.setLockedUntil(null);
        authLoginCacheService.cacheInternalUserLoginSnapshot(internalUser);

        String accessToken = jwtTokenUtils.generateAccessToken(internalUser.getId(), USER_TYPE);
        String refreshToken = jwtTokenUtils.generateRefreshToken(internalUser.getId(), USER_TYPE);
        Claims accessClaims = jwtTokenUtils.parseClaims(accessToken);
        Claims refreshClaims = jwtTokenUtils.parseClaims(refreshToken);
        securityCacheService.markTokenActive(accessToken, accessClaims.getExpiration());
        securityCacheService.markTokenActive(refreshToken, refreshClaims.getExpiration());

        ResponseCookie accessCookie = buildTokenCookie(
                ACCESS_TOKEN_COOKIE_NAME,
                accessToken,
                jwtProperties.getAccessExpirationSeconds(),
                httpServletRequest.isSecure()
        );
        ResponseCookie refreshCookie = buildTokenCookie(
                REFRESH_TOKEN_COOKIE_NAME,
                refreshToken,
                jwtProperties.getRefreshExpirationSeconds(),
                httpServletRequest.isSecure()
        );
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        AuthLoginResultVO loginResult = new AuthLoginResultVO();
        loginResult.setUserId(internalUser.getId());
        loginResult.setUserType(USER_TYPE);
        loginResult.setUsername(internalUser.getUsername());
        loginResult.setName(internalUser.getName());
        loginResult.setMustChangePassword(MustChangePasswordFlag.YES.equals(internalUser.getMustChangePasswordEnum()));
        return loginResult;
    }

    private InternalUserEntity loadInternalUserForLogin(String username) {
        InternalUserEntity snapshot = authLoginCacheService.getInternalUserLoginSnapshot(username);
        if (snapshot != null) {
            return snapshot;
        }
        InternalUserEntity internalUser = internalLoginMapper.selectInternalUserByUsername(username);
        if (internalUser != null) {
            authLoginCacheService.cacheInternalUserLoginSnapshot(internalUser);
        }
        return internalUser;
    }

    private ResponseCookie buildTokenCookie(String cookieName, String token, long maxAgeSeconds, boolean secure) {
        long normalizedMaxAge = Math.max(maxAgeSeconds, 1L);
        return ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(normalizedMaxAge)
                .sameSite("Lax")
                .build();
    }
}
