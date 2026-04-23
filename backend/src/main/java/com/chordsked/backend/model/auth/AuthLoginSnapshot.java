package com.chordsked.backend.model.auth;

import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.UserDataScopeType;

public abstract class AuthLoginSnapshot {
    private Long userId;
    private AccountUserType userType;
    private AuthLoginMethod loginMethod;
    private String principal;
    private String name;
    private Boolean enabled;
    private Boolean mustChangePassword;
    private UserDataScopeType dataScopeType;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AccountUserType getUserType() {
        return userType;
    }

    public void setUserType(AccountUserType userType) {
        this.userType = userType;
    }

    public AuthLoginMethod getLoginMethod() {
        return loginMethod;
    }

    public void setLoginMethod(AuthLoginMethod loginMethod) {
        this.loginMethod = loginMethod;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public UserDataScopeType getDataScopeType() {
        return dataScopeType;
    }

    public void setDataScopeType(UserDataScopeType dataScopeType) {
        this.dataScopeType = dataScopeType;
    }
}
