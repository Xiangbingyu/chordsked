package com.chordsked.backend.service.verification.support;

import com.chordsked.backend.model.auth.AuthLoginSnapshot;
import com.chordsked.backend.model.dto.auth.AuthLoginRequest;
import com.chordsked.backend.model.enums.AccountUserType;

public interface LoginSnapshotLoadService {
    <T extends AuthLoginSnapshot> T load(
            AuthLoginRequest request,
            AccountUserType userType,
            String principal,
            Class<T> snapshotType
    );
}
