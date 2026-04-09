package com.chordsked.backend.service.auth;

import com.chordsked.backend.model.dto.auth.AuthLoginRequest;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.vo.auth.AuthLoginResultVO;

public interface AuthLoginService {
    LoginExecutionResult login(
            AuthLoginRequest request,
            AccountUserType userType,
            boolean secureRequest
    );

    record LoginExecutionResult(
            AuthLoginResultVO loginResult,
            String accessToken,
            long accessTokenMaxAgeSeconds,
            String refreshToken,
            long refreshTokenMaxAgeSeconds,
            boolean secureCookie
    ) {
    }
}
