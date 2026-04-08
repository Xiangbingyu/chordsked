package com.chordsked.backend.service.verification.userstatus.provider;

import com.chordsked.backend.model.enums.AccountUserType;

public interface UserStatusVerificationProvider {
    AccountUserType getUserType();

    void verify(Long userId);
}
