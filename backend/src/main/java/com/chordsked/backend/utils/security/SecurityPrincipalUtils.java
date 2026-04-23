package com.chordsked.backend.utils.security;

import com.chordsked.backend.security.account.model.ChordSkedUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityPrincipalUtils {
    private SecurityPrincipalUtils() {
    }

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof ChordSkedUserDetails userDetails) {
            return userDetails.getUserId();
        }
        return null;
    }
}
