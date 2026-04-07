package com.chordsked.backend.service.verification.support.provider;

import com.chordsked.backend.model.auth.AuthLoginSnapshot;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.dto.auth.AuthLoginRequest;
import com.chordsked.backend.model.enums.AccountUserType;

public interface LoginSnapshotLoadProvider {
    AuthLoginMethod getLoginMethod();

    AccountUserType getUserType();

    AuthLoginSnapshot load(AuthLoginRequest request, String principal);
}
