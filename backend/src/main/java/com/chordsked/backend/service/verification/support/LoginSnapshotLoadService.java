package com.chordsked.backend.service.verification.support;

import com.chordsked.backend.model.auth.AuthLoginSnapshot;
import com.chordsked.backend.model.dto.auth.AuthLoginRequest;

public interface LoginSnapshotLoadService {
    <T extends AuthLoginSnapshot> T load(AuthLoginRequest request, String principal, Class<T> snapshotType);
}
