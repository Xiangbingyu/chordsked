package com.chordsked.backend.model.auth;

public class UsernamePasswordSmsCodeLoginSnapshot extends AuthLoginSnapshot {
    private String password;
    private String phone;

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
}
