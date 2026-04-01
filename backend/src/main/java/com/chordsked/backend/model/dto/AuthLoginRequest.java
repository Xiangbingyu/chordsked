package com.chordsked.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "AuthLoginRequest", description = "登录请求参数")
public class AuthLoginRequest {
    @Schema(description = "用户名", example = "admin")
    @NotBlank
    private String username;

    @Schema(description = "密码", example = "Aa123456!")
    @NotBlank
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
