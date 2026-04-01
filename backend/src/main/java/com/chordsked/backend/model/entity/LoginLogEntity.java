package com.chordsked.backend.model.entity;

import com.chordsked.backend.model.enums.LoginLogResult;
import com.chordsked.backend.model.enums.LoginLogUserType;

import java.time.LocalDateTime;

public class LoginLogEntity {
    private Long id;
    private Long userId;
    private String username;
    private LoginLogUserType userType;
    private String ip;
    private String userAgent;
    private LoginLogResult result;
    private String message;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserType() { return userType == null ? null : userType.name(); }
    public void setUserType(String userType) { this.userType = LoginLogUserType.fromValue(userType); }
    public LoginLogUserType getUserTypeEnum() { return userType; }
    public void setUserTypeEnum(LoginLogUserType userType) { this.userType = userType; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public Integer getResult() { return result == null ? null : result.getCode(); }
    public void setResult(Integer result) { this.result = LoginLogResult.fromCode(result); }
    public LoginLogResult getResultEnum() { return result; }
    public void setResultEnum(LoginLogResult result) { this.result = result; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
