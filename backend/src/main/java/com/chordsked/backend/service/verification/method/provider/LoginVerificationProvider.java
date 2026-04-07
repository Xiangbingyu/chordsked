package com.chordsked.backend.service.verification.method.provider;

import com.chordsked.backend.model.dto.auth.AuthLoginRequest;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.vo.auth.AuthLoginResultVO;

public interface LoginVerificationProvider {
    AuthLoginMethod getLoginMethod();

    AuthLoginResultVO verify(AuthLoginRequest request);
}
