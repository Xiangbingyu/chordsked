package com.chordsked.backend.service.security;

import com.chordsked.backend.model.enums.AccountUserType;

import java.util.List;

public interface AuthorityCodeService {
    List<String> getAuthorityCodes(AccountUserType userType, Long userId);
}
