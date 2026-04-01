package com.chordsked.backend.model.enums;

public enum PermissionResourceType {
    MENU(1),
    BUTTON(2),
    DATA(3);

    private final int code;

    PermissionResourceType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PermissionResourceType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PermissionResourceType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
