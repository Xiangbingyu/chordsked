package com.chordsked.backend.model.enums;

public enum CampusStatus {
    DISABLED(0),
    ENABLED(1);

    private final int code;

    CampusStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CampusStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CampusStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
