package com.chordsked.backend.model.enums;

public enum InternalUserStatus {
    DISABLED(0),
    ENABLED(1);

    private final int code;

    InternalUserStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static InternalUserStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (InternalUserStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
