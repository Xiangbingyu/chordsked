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
        for (PermissionStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
