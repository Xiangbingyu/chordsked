package com.chordsked.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "InternalAuthLoginRequest", description = "教务端登录请求参数")
public class InternalAuthLoginRequest {
    @Schema(description = "用户名，最长 50 位且不能包含空白字符", example = "admin")
    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^\\S+$")
    private String username;

    @Schema(description = "密码，最长 100 位", example = "Aa123456!")
    @NotBlank
    @Size(max = 100)
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
