package com.chordsked.backend.service.internaluser;

import com.chordsked.backend.model.dto.internaluser.InternalUserStatusUpdateRequest;

public interface InternalUserStatusUpdateService {
    void updateStatus(InternalUserStatusUpdateRequest request);
}
