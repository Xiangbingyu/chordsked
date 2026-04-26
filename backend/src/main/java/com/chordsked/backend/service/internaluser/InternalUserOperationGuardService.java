package com.chordsked.backend.service.internaluser;

import com.chordsked.backend.model.entity.InternalUserEntity;

public interface InternalUserOperationGuardService {
    InternalUserEntity validateOperationTarget(Long targetUserId, String actionName);

    boolean isProtectedUser(Long userId);
}
