package com.chordsked.backend.service.verification.method;

import com.chordsked.backend.model.dto.auth.AuthLoginRequest;
import com.chordsked.backend.model.vo.auth.AuthLoginResultVO;

public interface LoginMethodVerificationService {
    AuthLoginResultVO verify(AuthLoginRequest request);
}
