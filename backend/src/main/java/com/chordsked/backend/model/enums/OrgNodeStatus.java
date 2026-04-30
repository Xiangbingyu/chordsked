package com.chordsked.backend.model.enums;

public enum OrgNodeStatus {
    DISABLED(0),
    ENABLED(1),
    DELETED(2);

    private final int code;

    OrgNodeStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static OrgNodeStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrgNodeStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
