package com.chordsked.backend.model.vo.auth;

import com.chordsked.backend.model.enums.AccountUserType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthRefreshResultVO", description = "刷新登录结果")
public class AuthRefreshResultVO {
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户类型", allowableValues = {"ADMIN", "TEACHER", "STUDENT"})
    private AccountUserType userType;

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
}
