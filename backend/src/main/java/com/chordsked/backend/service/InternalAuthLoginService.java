package com.chordsked.backend.service;

import com.chordsked.backend.model.dto.InternalAuthLoginRequest;
import com.chordsked.backend.model.vo.InternalAuthLoginResultVO;

public interface InternalAuthLoginService {
    LoginExecutionResult login(
            InternalAuthLoginRequest request,
            boolean secureRequest
    );

    record LoginExecutionResult(
            InternalAuthLoginResultVO loginResult,
            String accessToken,
            long accessTokenMaxAgeSeconds,
            String refreshToken,
            long refreshTokenMaxAgeSeconds,
            boolean secureCookie
    ) {
    }
}
