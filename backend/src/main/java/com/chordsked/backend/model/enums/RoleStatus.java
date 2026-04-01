package com.chordsked.backend.model.enums;

public enum RoleStatus {
    DISABLED(0),
    ENABLED(1);

    private final int code;

    RoleStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static RoleStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RoleStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
