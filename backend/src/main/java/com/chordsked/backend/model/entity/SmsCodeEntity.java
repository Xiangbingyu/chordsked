package com.chordsked.backend.model.entity;

import java.time.LocalDateTime;

public class SmsCodeEntity {
    private Long id;
    private String phone;
    private String code;
    private String scene;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
