package com.chordsked.backend.model.enums;

public enum PermissionType {
    MENU(1),
    BUTTON(2);

    private final int code;

    PermissionType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PermissionType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PermissionType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
