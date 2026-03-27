package com.chordsked.backend.security.account.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class ChordSkedUserDetails implements UserDetails {
    private final String userId;
    private final String userType;
    private final Long currentCampusId;
    private final Collection<? extends GrantedAuthority> authorities;

    public ChordSkedUserDetails(
            String userId,
            String userType,
            Long currentCampusId,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.userId = userId;
        this.userType = userType;
        this.currentCampusId = currentCampusId;
        this.authorities = authorities;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserType() {
        return userType;
    }

    public Long getCurrentCampusId() {
        return currentCampusId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return userId;
    }
}
