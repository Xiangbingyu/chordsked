package com.chordsked.backend.controller;

import com.chordsked.backend.common.ApiResponse;
import com.chordsked.backend.model.dto.InternalAuthLoginRequest;
import com.chordsked.backend.model.vo.InternalAuthLoginResultVO;
import com.chordsked.backend.service.InternalAuthLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1")
@Validated
@Tag(name = "认证模块", description = "登录认证接口")
public class InternalAuthController {
    @Resource(name = "internalAuthLoginService")
    private InternalAuthLoginService internalAuthLoginService;

    @PostMapping("/login")
    @Operation(summary = "教务端登录", description = "校验账号密码并通过 HttpOnly Cookie 下发 access token 与 refresh token")
    public ApiResponse<InternalAuthLoginResultVO> login(
            @Valid @RequestBody InternalAuthLoginRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        InternalAuthLoginService.LoginExecutionResult loginExecutionResult =
                internalAuthLoginService.login(request, httpServletRequest.isSecure());
        httpServletResponse.addHeader(
                HttpHeaders.SET_COOKIE,
                buildTokenCookie(
                        "access_token",
                        loginExecutionResult.accessToken(),
                        loginExecutionResult.accessTokenMaxAgeSeconds(),
                        loginExecutionResult.secureCookie()
                ).toString()
        );
        httpServletResponse.addHeader(
                HttpHeaders.SET_COOKIE,
                buildTokenCookie(
                        "refresh_token",
                        loginExecutionResult.refreshToken(),
                        loginExecutionResult.refreshTokenMaxAgeSeconds(),
                        loginExecutionResult.secureCookie()
                ).toString()
        );
        return ApiResponse.success(loginExecutionResult.loginResult());
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
