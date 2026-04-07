package com.chordsked.backend.controller;

import com.chordsked.backend.common.ApiResponse;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.dto.auth.AuthLoginRequest;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.vo.auth.AuthLoginResultVO;
import com.chordsked.backend.service.auth.AuthLoginService;
import com.chordsked.backend.utils.cookie.CookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一登录入口。
 * 控制器仅负责校验访问端与请求中的用户类型是否一致，并将请求转交登录编排服务处理。
 */
@RestController
@RequestMapping({"/admin/api/v1", "/teachers/api/v1", "/students/api/v1"})
@Validated
@Tag(name = "认证模块", description = "登录认证接口")
public class AuthController {
    @Resource(name = "authLoginService")
    private AuthLoginService authLoginService;

    @PostMapping("/login")
    @Operation(summary = "统一登录", description = "请求体必须显式携带 userType，且必须与当前访问端路由一致；登录成功后通过 HttpOnly Cookie 下发 access token 与 refresh token")
    public ApiResponse<AuthLoginResultVO> login(
            @Valid @RequestBody AuthLoginRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        request.setUserType(resolveUserType(request.getUserType(), httpServletRequest.getRequestURI()));
        request.setLoginMethod(resolveLoginMethod(request));
        validateLoginRequest(request);
        AuthLoginService.LoginExecutionResult loginExecutionResult =
                authLoginService.login(request, httpServletRequest.isSecure());
        CookieUtils.writeCookie(
                httpServletResponse,
                CookieUtils.ACCESS_TOKEN_COOKIE_NAME,
                loginExecutionResult.accessToken(),
                loginExecutionResult.accessTokenMaxAgeSeconds(),
                loginExecutionResult.secureCookie()
        );
        CookieUtils.writeCookie(
                httpServletResponse,
                CookieUtils.REFRESH_TOKEN_COOKIE_NAME,
                loginExecutionResult.refreshToken(),
                loginExecutionResult.refreshTokenMaxAgeSeconds(),
                loginExecutionResult.secureCookie()
        );
        return ApiResponse.success(loginExecutionResult.loginResult());
    }

    /**
     * 校验请求声明的用户类型是否与当前访问路由一致。
     * controller 不再负责推断默认用户类型，避免不同端请求在入口层出现歧义。
     */
    private AccountUserType resolveUserType(AccountUserType requestUserType, String requestUri) {
        if (requestUserType == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录用户类型不能为空");
        }
        if (requestUri == null || requestUri.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录请求路由无效");
        }
        AccountUserType routeUserType;
        if (requestUri.startsWith("/admin/api/v1/")) {
            routeUserType = AccountUserType.ADMIN;
        } else if (requestUri.startsWith("/teachers/api/v1/")) {
            routeUserType = AccountUserType.TEACHER;
        } else if (requestUri.startsWith("/students/api/v1/")) {
            routeUserType = AccountUserType.STUDENT;
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录请求路由无效");
        }
        if (!routeUserType.equals(requestUserType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录用户类型与访问端不匹配");
        }
        return requestUserType;
    }
    

    /**
     * 校验请求声明的登录方式是否与请求体中其他字段一致。
     * controller 不再负责推断默认登录方式，避免不同端请求在入口层出现歧义。
     */
    private AuthLoginMethod resolveLoginMethod(AuthLoginRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录请求参数不合法");
        }
        if (request.getLoginMethod() != null) {
            return request.getLoginMethod();
        }
        boolean hasUsername = StringUtils.hasText(request.getUsername());
        boolean hasPassword = StringUtils.hasText(request.getPassword());
        boolean hasPhone = StringUtils.hasText(request.getPhone());
        boolean hasSmsCode = StringUtils.hasText(request.getSmsCode());
        if (hasUsername && hasPassword && !hasPhone && !hasSmsCode) {
            return AuthLoginMethod.USERNAME_PASSWORD;
        }
        if (!hasUsername && !hasPassword && hasPhone && hasSmsCode) {
            return AuthLoginMethod.PHONE_SMS_CODE;
        }
        if (hasUsername && hasPassword && hasSmsCode) {
            return AuthLoginMethod.USERNAME_PASSWORD_SMS_CODE;
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "登录请求参数不合法");
    }

    /**
     * 按登录方式校验条件必填字段。
     * 这里只负责入口层“字段是否齐全”的校验，具体账号状态、密码比对、快照装载仍由 service/verification 链路处理。
     */
    private void validateLoginRequest(AuthLoginRequest request) {
        if (AuthLoginMethod.USERNAME_PASSWORD.equals(request.getLoginMethod())) {
            if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名和密码不能为空");
            }
            return;
        }
        if (AuthLoginMethod.PHONE_SMS_CODE.equals(request.getLoginMethod())) {
            if (!StringUtils.hasText(request.getPhone()) || !StringUtils.hasText(request.getSmsCode())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "手机号和短信验证码不能为空");
            }
            return;
        }
        if (AuthLoginMethod.USERNAME_PASSWORD_SMS_CODE.equals(request.getLoginMethod())
                && (!StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getPassword())
                || !StringUtils.hasText(request.getSmsCode()))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名、密码和短信验证码不能为空");
        }
    }
}
