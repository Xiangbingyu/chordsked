package com.chordsked.backend.service.auth.impl;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.config.properties.JwtProperties;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.auth.AuthLoginRequest;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.vo.auth.AuthLoginResultVO;
import com.chordsked.backend.service.auth.AuthLoginService;
import com.chordsked.backend.service.verification.method.LoginMethodVerificationService;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 登录编排服务。
 * 负责识别登录方式、调用凭证验证服务、签发 token 并组装统一登录结果。
 */
@Service("authLoginService")
public class AuthLoginServiceImpl implements AuthLoginService {
    @Resource(name = "loginMethodVerificationService")
    private LoginMethodVerificationService loginMethodVerificationService;

    @Resource(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @Resource(name = "jwtTokenUtils")
    private JwtTokenUtils jwtTokenUtils;

    @Resource(name = "jwtProperties")
    private JwtProperties jwtProperties;

    /**
     * 执行统一登录流程。
     * 该方法不直接处理具体凭证校验细节，而是委托 verification 模块按登录方式完成验证。
     */
    @Override
    public LoginExecutionResult login(AuthLoginRequest request, AccountUserType userType, boolean secureRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录请求参数不合法");
        }
        if (userType == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录用户类型不能为空");
        }
        /**
         * 当前 HTTP 登录入口会在 controller 层完成 loginMethod 标准化，
         * service 这里只校验编排输入是否完整，不再重复解析登录方式。
         */
        if (request.getLoginMethod() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录方式不能为空");
        }

        AuthLoginResultVO loginResult = loginMethodVerificationService.verify(request, userType);

        /**
         * 登录校验通过后立即签发 access/refresh token，并把 token 状态写入安全缓存，
         * 便于后续鉴权链路统一做 token 激活状态校验。
         */
        String accessToken = jwtTokenUtils.generateAccessToken(loginResult.getUserId(), loginResult.getUserType().getCode());
        String refreshToken = jwtTokenUtils.generateRefreshToken(loginResult.getUserId(), loginResult.getUserType().getCode());
        Claims accessClaims = jwtTokenUtils.parseClaims(accessToken);
        Claims refreshClaims = jwtTokenUtils.parseClaims(refreshToken);
        securityCacheService.markTokenActive(accessToken, accessClaims.getExpiration());
        securityCacheService.markTokenActive(refreshToken, refreshClaims.getExpiration());
        return new LoginExecutionResult(
                loginResult,
                accessToken,
                jwtProperties.getAccessExpirationSeconds(),
                refreshToken,
                jwtProperties.getRefreshExpirationSeconds(),
                secureRequest
        );
    }

}
