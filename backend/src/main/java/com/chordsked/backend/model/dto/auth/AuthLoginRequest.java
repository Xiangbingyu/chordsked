package com.chordsked.backend.model.dto.auth;

import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "AuthLoginRequest", description = "统一登录请求参数")
public class AuthLoginRequest {
    @Schema(
            description = "用户类型，必须与访问路由端一致：admin 对应 ADMIN、teachers 对应 TEACHER、students 对应 STUDENT",
            allowableValues = {"ADMIN", "TEACHER", "STUDENT"},
            example = "ADMIN",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private AccountUserType userType;

    @Schema(
            description = "登录方式，可选；未传时根据请求字段自动识别。ADMIN 仅支持 USERNAME_PASSWORD，STUDENT 仅支持 PHONE_SMS_CODE，TEACHER 支持 USERNAME_PASSWORD 与 PHONE_SMS_CODE",
            allowableValues = {"USERNAME_PASSWORD", "PHONE_SMS_CODE", "USERNAME_PASSWORD_SMS_CODE"},
            example = "USERNAME_PASSWORD"
    )
    private AuthLoginMethod loginMethod;

    @Schema(description = "用户名，适用于账号密码登录或账号密码验证码登录，最长 50 位且不能包含空白字符", example = "admin")
    @Size(max = 50)
    @Pattern(regexp = "^\\S+$")
    private String username;

    @Schema(description = "密码，适用于账号密码登录或账号密码验证码登录，最长 100 位", example = "Aa123456!")
    @Size(max = 100)
    private String password;

    @Schema(description = "手机号，适用于手机号验证码登录，最长 20 位", example = "13800138000")
    @Size(max = 20)
    private String phone;

    @Schema(description = "短信验证码，适用于手机号验证码登录或账号密码验证码登录，最长 10 位", example = "123456")
    @Size(max = 10)
    private String smsCode;

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }
}
