package com.chordsked.backend.service.internaluser;

import com.chordsked.backend.model.dto.internaluser.InternalUserUpdateRequest;

public interface InternalUserUpdateService {
    void update(InternalUserUpdateRequest request);
}
