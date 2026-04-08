package com.chordsked.backend.service.verification.userstatus;

import com.chordsked.backend.model.enums.AccountUserType;

public interface UserStatusVerificationService {
    void verify(Long userId, AccountUserType userType);
}
