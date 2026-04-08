package com.chordsked.backend.service.auth;

import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.vo.auth.AuthRefreshResultVO;

public interface AuthRefreshService {
    RefreshExecutionResult refreshToken(
            Long userId,
            AccountUserType userType,
            String accessToken,
            String refreshToken,
            boolean secureRequest
    );

    record RefreshExecutionResult(
            AuthRefreshResultVO refreshResult,
            String accessToken,
            long accessTokenMaxAgeSeconds,
            String refreshToken,
            long refreshTokenMaxAgeSeconds,
            boolean secureCookie
    ) {
    }
}
