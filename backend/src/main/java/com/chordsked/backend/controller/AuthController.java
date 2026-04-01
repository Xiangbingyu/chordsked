package com.chordsked.backend.controller;

import com.chordsked.backend.common.ApiResponse;
import com.chordsked.backend.model.dto.AuthLoginRequest;
import com.chordsked.backend.model.vo.AuthLoginResultVO;
import com.chordsked.backend.service.AuthLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@Tag(name = "认证模块", description = "登录认证接口")
public class AuthController {
    @Resource(name = "authLoginService")
    private AuthLoginService authLoginService;

    @PostMapping("/login")
    @Operation(summary = "教务端登录")
    public ApiResponse<AuthLoginResultVO> login(
            @Valid @RequestBody AuthLoginRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        return ApiResponse.success(authLoginService.login(request, httpServletRequest, httpServletResponse));
    }
}
