package com.chordsked.backend.model.enums;

public enum LoginLogResult {
    FAILURE(0),
    SUCCESS(1);

    private final int code;

    LoginLogResult(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static LoginLogResult fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LoginLogResult value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
