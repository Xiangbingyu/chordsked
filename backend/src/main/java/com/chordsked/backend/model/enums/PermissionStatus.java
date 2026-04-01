package com.chordsked.backend.model.enums;

public enum PermissionStatus {
    DISABLED(0),
    ENABLED(1);

    private final int code;

    PermissionStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PermissionStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PermissionStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
