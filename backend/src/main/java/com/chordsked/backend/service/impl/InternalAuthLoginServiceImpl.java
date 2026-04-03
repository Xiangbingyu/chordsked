package com.chordsked.backend.service.impl;

import com.chordsked.backend.cache.InternalAuthLoginCacheService;
import com.chordsked.backend.cache.SecurityCacheService;
import com.chordsked.backend.config.properties.InternalAuthLoginProperties;
import com.chordsked.backend.config.properties.JwtProperties;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.InternalAuthLoginRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.InternalUserStatus;
import com.chordsked.backend.model.enums.MustChangePasswordFlag;
import com.chordsked.backend.model.vo.InternalAuthLoginResultVO;
import com.chordsked.backend.service.InternalAuthLoginService;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("internalAuthLoginService")
public class InternalAuthLoginServiceImpl implements InternalAuthLoginService {
    private static final Logger logger = LoggerFactory.getLogger(InternalAuthLoginServiceImpl.class);
    private static final AccountUserType USER_TYPE = AccountUserType.ADMIN;

    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Resource(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @Resource(name = "internalAuthLoginCacheService")
    private InternalAuthLoginCacheService internalAuthLoginCacheService;

    @Resource(name = "jwtTokenUtils")
    private JwtTokenUtils jwtTokenUtils;

    @Resource(name = "jwtProperties")
    private JwtProperties jwtProperties;

    @Resource(name = "internalAuthLoginProperties")
    private InternalAuthLoginProperties internalAuthLoginProperties;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 执行内部账号登录：
     * 1) 校验账号状态与锁定状态；
     * 2) 校验密码并维护失败次数；
     * 3) 签发 access/refresh token；
     * 4) 将 token 状态写入安全缓存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginExecutionResult login(
            InternalAuthLoginRequest request,
            boolean secureRequest
    ) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("login request is invalid");
        }
        String username = request.getUsername().trim();
        InternalUserEntity internalUser = loadInternalUserForLogin(username);
        if (internalUser == null) {
            auditLoginFailure(username, "user_not_found");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (!InternalUserStatus.ENABLED.equals(internalUser.getStatusEnum())) {
            auditLoginFailure(username, "user_disabled");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号已被禁用，请联系管理员");
        }
        Long lockedUntil = internalAuthLoginCacheService.getLockedUntil(username);
        if (lockedUntil != null && lockedUntil > System.currentTimeMillis()) {
            auditLoginFailure(username, "user_locked");
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED,
                    "账号已锁定，请 " + internalAuthLoginProperties.getFailLockMinutes() + " 分钟后重试"
            );
        }
        if (!passwordEncoder.matches(request.getPassword(), internalUser.getPassword())) {
            internalAuthLoginCacheService.recordLoginFailure(
                    username,
                    internalAuthLoginProperties.getFailLockThreshold(),
                    internalAuthLoginProperties.getFailLockMinutes()
            );
            auditLoginFailure(username, "password_mismatch");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }

        internalAuthLoginCacheService.clearLoginState(username);
        internalAuthLoginCacheService.cacheInternalUserLoginSnapshot(internalUser);

        String accessToken = jwtTokenUtils.generateAccessToken(internalUser.getId(), USER_TYPE.getCode());
        String refreshToken = jwtTokenUtils.generateRefreshToken(internalUser.getId(), USER_TYPE.getCode());
        Claims accessClaims = jwtTokenUtils.parseClaims(accessToken);
        Claims refreshClaims = jwtTokenUtils.parseClaims(refreshToken);
        securityCacheService.markTokenActive(accessToken, accessClaims.getExpiration());
        securityCacheService.markTokenActive(refreshToken, refreshClaims.getExpiration());

        InternalAuthLoginResultVO loginResult = new InternalAuthLoginResultVO();
        loginResult.setUserId(internalUser.getId());
        loginResult.setUserType(USER_TYPE);
        loginResult.setUsername(internalUser.getUsername());
        loginResult.setName(internalUser.getName());
        loginResult.setMustChangePassword(MustChangePasswordFlag.YES.equals(internalUser.getMustChangePasswordEnum()));
        logger.info("Internal login succeeded, userId={}, username={}", internalUser.getId(), username);
        return new LoginExecutionResult(
                loginResult,
                accessToken,
                jwtProperties.getAccessExpirationSeconds(),
                refreshToken,
                jwtProperties.getRefreshExpirationSeconds(),
                secureRequest
        );
    }

    /**
     * 优先从登录快照缓存读取内部账号；缓存未命中时回源数据库并回填缓存。
     */
    private InternalUserEntity loadInternalUserForLogin(String username) {
        InternalUserEntity snapshot = internalAuthLoginCacheService.getInternalUserLoginSnapshot(username);
        if (snapshot != null) {
            return snapshot;
        }
        InternalUserEntity internalUser = internalUserDao.getByUsername(username);
        if (internalUser != null) {
            internalAuthLoginCacheService.cacheInternalUserLoginSnapshot(internalUser);
        }
        return internalUser;
    }

    /**
     * 记录登录失败审计日志。
     * 仅记录 username 与失败原因，避免输出明文密码等敏感信息。
     */
    private void auditLoginFailure(String username, String reason) {
        logger.warn("Internal login failed, username={}, reason={}", username, reason);
    }
}
