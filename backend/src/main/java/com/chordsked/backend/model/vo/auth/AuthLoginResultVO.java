package com.chordsked.backend.model.vo.auth;

import com.chordsked.backend.model.enums.AccountUserType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthLoginResultVO", description = "统一登录结果")
public class AuthLoginResultVO {
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户类型", allowableValues = {"ADMIN", "TEACHER", "STUDENT"})
    private AccountUserType userType;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "是否需要修改密码")
    private Boolean mustChangePassword;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}
