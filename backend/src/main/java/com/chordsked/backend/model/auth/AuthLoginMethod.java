package com.chordsked.backend.model.auth;

import com.chordsked.backend.utils.normalize.StringNormalizeUtils;

public enum AuthLoginMethod {
    USERNAME_PASSWORD("USERNAME_PASSWORD"),
    PHONE_SMS_CODE("PHONE_SMS_CODE"),
    USERNAME_PASSWORD_SMS_CODE("USERNAME_PASSWORD_SMS_CODE");

    private final String code;

    AuthLoginMethod(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AuthLoginMethod fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String normalizedCode = StringNormalizeUtils.normalizeOrEmpty(code);
        for (AuthLoginMethod method : values()) {
            if (method.code.equals(normalizedCode)) {
                return method;
            }
        }
        return null;
    }
}

