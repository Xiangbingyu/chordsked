package com.chordsked.backend.service.verification.method.provider;

import com.chordsked.backend.cache.auth.AuthLoginStateCacheService;
import com.chordsked.backend.config.properties.AuthLoginProperties;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.auth.UsernamePasswordLoginSnapshot;
import com.chordsked.backend.model.dto.auth.AuthLoginRequest;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.vo.auth.AuthLoginResultVO;
import com.chordsked.backend.service.verification.support.LoginSnapshotLoadService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 账号密码登录校验 provider。
 * 负责完成用户名密码登录链路中的参数校验、快照装载、失败锁定控制、密码比对与登录结果组装。
 */
@Component("usernamePasswordLoginVerificationProvider")
public class UsernamePasswordLoginVerificationProvider implements LoginVerificationProvider {
    private static final Logger logger = LoggerFactory.getLogger(UsernamePasswordLoginVerificationProvider.class);

    @Resource(name = "authLoginStateCacheService")
    private AuthLoginStateCacheService authLoginStateCacheService;

    @Resource(name = "loginSnapshotLoadService")
    private LoginSnapshotLoadService loginSnapshotLoadService;

    @Resource(name = "authLoginProperties")
    private AuthLoginProperties authLoginProperties;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AuthLoginMethod getLoginMethod() {
        return AuthLoginMethod.USERNAME_PASSWORD;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 用户名密码登录主流程。
     * 该方法只处理“登录方式”层面的通用校验逻辑，账号来源与缓存装载由 support 模块负责。
     */
    public AuthLoginResultVO verify(AuthLoginRequest request, AccountUserType userType) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录请求参数不合法");
        }
        if (userType == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录用户类型不能为空");
        }
        String principal = request.getUsername().trim();
        if (principal.isEmpty() || request.getPassword().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录请求参数不合法");
        }
        request.setUsername(principal);
        /**
         * 从 support 模块装载当前登录方式所需快照。
         * support 层会先查快照缓存，未命中时再回源账号数据并回填缓存。
         */
        UsernamePasswordLoginSnapshot loginSnapshot =
                loginSnapshotLoadService.load(request, userType, principal, UsernamePasswordLoginSnapshot.class);
        if (loginSnapshot == null) {
            auditLoginFailure(userType.getCode(), principal, "user_not_found");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (!Boolean.TRUE.equals(loginSnapshot.getEnabled())) {
            auditLoginFailure(userType.getCode(), principal, "user_disabled");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号已被禁用，请联系管理员");
        }
        Long lockedUntil = authLoginStateCacheService.getLockedUntil(userType, principal);
        if (lockedUntil != null && lockedUntil > System.currentTimeMillis()) {
            auditLoginFailure(userType.getCode(), principal, "user_locked");
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED,
                    "账号已锁定，请 " + authLoginProperties.getFailLockMinutes() + " 分钟后重试"
            );
        }
        if (!passwordEncoder.matches(request.getPassword(), loginSnapshot.getPassword())) {
            authLoginStateCacheService.recordLoginFailure(
                    userType,
                    principal,
                    authLoginProperties.getFailLockThreshold(),
                    authLoginProperties.getFailLockMinutes()
            );
            auditLoginFailure(userType.getCode(), principal, "password_mismatch");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        authLoginStateCacheService.clearLoginState(userType, principal);
        logger.info(
                "Login succeeded, userType={}, userId={}, principal={}",
                userType.getCode(),
                loginSnapshot.getUserId(),
                principal
        );
        AuthLoginResultVO loginResult = new AuthLoginResultVO();
        loginResult.setUserId(loginSnapshot.getUserId());
        loginResult.setUserType(loginSnapshot.getUserType());
        loginResult.setUsername(loginSnapshot.getPrincipal());
        loginResult.setName(loginSnapshot.getName());
        loginResult.setMustChangePassword(Boolean.TRUE.equals(loginSnapshot.getMustChangePassword()));
        return loginResult;
    }

    /**
     * 统一记录登录失败原因，便于后续审计和问题排查。
     */
    private void auditLoginFailure(String userType, String principal, String reason) {
        logger.warn("Login failed, userType={}, principal={}, reason={}", userType, principal, reason);
    }
}
