package com.chordsked.backend.service.internaluser;

import com.chordsked.backend.model.dto.internaluser.InternalUserResetPasswordRequest;

public interface InternalUserResetPasswordService {
    void resetPassword(InternalUserResetPasswordRequest request);
}
