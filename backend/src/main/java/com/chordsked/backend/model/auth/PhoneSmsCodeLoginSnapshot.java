package com.chordsked.backend.model.auth;

public class PhoneSmsCodeLoginSnapshot extends AuthLoginSnapshot {
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
