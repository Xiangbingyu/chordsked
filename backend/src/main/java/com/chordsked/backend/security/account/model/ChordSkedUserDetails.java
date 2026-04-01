package com.chordsked.backend.security.account.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class ChordSkedUserDetails implements UserDetails {
    private final Long userId;
    private final String userType;
    private final Long currentCampusId;
    private final boolean accountNonLocked;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public ChordSkedUserDetails(
            Long userId,
            String userType,
            Long currentCampusId,
            boolean accountNonLocked,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.userId = userId;
        this.userType = userType;
        this.currentCampusId = currentCampusId;
        this.accountNonLocked = accountNonLocked;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    public Long getUserId() {
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
        return userType + ":" + userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
